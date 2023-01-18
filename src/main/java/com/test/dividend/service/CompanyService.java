package com.test.dividend.service;

import com.test.dividend.model.Company;
import com.test.dividend.model.ScrapedResult;
import com.test.dividend.persist.entity.CompanyEntity;
import com.test.dividend.persist.entity.DividendEntity;
import com.test.dividend.persist.repository.CompanyRepository;
import com.test.dividend.persist.repository.DividendRepository;
import com.test.dividend.scraper.Scraper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
//@AllArgsConstructor
@RequiredArgsConstructor
public class CompanyService {

  private final Scraper yahooFinanceScrapper;
  private final CompanyRepository companyRepository;
  private final DividendRepository dividendRepository;

  public Company save(String ticker) {
    boolean exists = this.companyRepository.existsByTicker(ticker);
    if (exists) {
      throw new RuntimeException("already exists ticker ->" + ticker);
    }
    return this.storeCompanyAndDividend(ticker);
  }


  public Page<CompanyEntity> getAllCompany(Pageable pageable) {
    return this.companyRepository.findAll(pageable);
  }

  private Company storeCompanyAndDividend(String ticker) {

    //ticker를 기준으로 회사 스크래핑
    Company company = this.yahooFinanceScrapper.scrapCompanyByTicker(ticker);
    if (ObjectUtils.isEmpty(company)) {
      throw new RuntimeException("failed to scrap ticker -> " + ticker);
    }

    //해당 회사가 존재할 경우, 회사 배당금 정보를 스크래핑
    ScrapedResult scrapedResult = this.yahooFinanceScrapper.scrap(company);

    //스크래핑 결과
    CompanyEntity companyEntity = this.companyRepository.save(new CompanyEntity(company));
    List<DividendEntity> dividendEntities = scrapedResult.getDividends().stream()
      .map(e -> new DividendEntity(companyEntity.getId(), e))
      .collect(Collectors.toList());

    this.dividendRepository.saveAll(dividendEntities);
    return company;
  }

}
