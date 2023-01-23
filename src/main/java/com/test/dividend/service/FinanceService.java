package com.test.dividend.service;

import com.test.dividend.exception.impl.NoCompanyException;
import com.test.dividend.model.Company;
import com.test.dividend.model.Dividend;
import com.test.dividend.model.ScrapedResult;
import com.test.dividend.model.constants.CacheKey;
import com.test.dividend.persist.entity.CompanyEntity;
import com.test.dividend.persist.entity.DividendEntity;
import com.test.dividend.persist.repository.CompanyRepository;
import com.test.dividend.persist.repository.DividendRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

  private final CompanyRepository companyRepository;
  private final DividendRepository dividendRepository;

  @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
  public ScrapedResult getDividendByCompanyName(String companyName) {

    log.info("search company ->", companyName);

    //1. 회사명 기준으로 회사 정보 조회
    CompanyEntity company = this.companyRepository.findByName(companyName)
        .orElseThrow(() -> new NoCompanyException());

    //2. 조회된 회사 id로 배당금 조회
    List<DividendEntity> dividendEntities =  this.dividendRepository.findAllByCompanyId(company.getId());

    //3. 결과 조합후 반환
    List<Dividend> dividends = dividendEntities.stream()
        .map(e -> new Dividend(e.getDate(), e.getDividend()))
        .collect(Collectors.toList());

    return new ScrapedResult(new Company(company.getTicker(), company.getName()),
                            dividends);
  }

}
