package com.test.dividend.service;

import com.test.dividend.exception.impl.NoCompanyException;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.Trie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@AllArgsConstructor
@Slf4j
public class CompanyService {

  private final Trie trie;

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

  public List<String> getCompanyNamesByKeyword(String keyword) {

    Pageable limit = PageRequest.of(0, 10);
    Page<CompanyEntity> companyEntities = this.companyRepository.findByNameStartingWithIgnoreCase(keyword, limit);
    return companyEntities.stream()
                    .map(e -> e.getName())
                    .collect(Collectors.toList());
  }

  public void addAutocompleteKeyword(String keyword) {
    //apach 라이브러리에서 지원하는 trie는 key, value 로 data set할 수 있는 기능도 지원하지만 사용하지 않기로 함
    this.trie.put(keyword, null);
  }

  public List<String> autocomplete(String keyword) {

    return (List<String>) this.trie.prefixMap(keyword).keySet()
        .stream()
        .limit(8)
        .collect(Collectors.toList());
  }

  public void deleteAutocompleteKeyword(String keyword) {
    this.trie.remove(keyword);
  }

  public String deleteCompany(String ticker) {

    var company = this.companyRepository.findByTicker(ticker)
        .orElseThrow(() -> new NoCompanyException());

    this.dividendRepository.deleteAllByCompanyId(company.getId());
    this.companyRepository.delete(company);

    //trie data delete
    this.deleteAutocompleteKeyword(company.getName());
    log.info("delete company Name: {} companyId: {}--->> ", company.getName(), company.getId());
    return company.getName();
  }
}
