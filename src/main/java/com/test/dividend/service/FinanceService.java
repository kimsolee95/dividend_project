package com.test.dividend.service;

import com.test.dividend.model.Company;
import com.test.dividend.model.Dividend;
import com.test.dividend.model.ScrapedResult;
import com.test.dividend.persist.entity.CompanyEntity;
import com.test.dividend.persist.entity.DividendEntity;
import com.test.dividend.persist.repository.CompanyRepository;
import com.test.dividend.persist.repository.DividendRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FinanceService {

  private final CompanyRepository companyRepository;
  private final DividendRepository dividendRepository;

  public ScrapedResult getDividendByCompanyName(String companyName) {

    //1. 회사명 기준으로 회사 정보 조회
    CompanyEntity company = this.companyRepository.findByName(companyName)
        .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명입니다."));

    //2. 조회된 회사 id로 배당금 조회
    List<DividendEntity> dividendEntities =  this.dividendRepository.findAllByCompanyId(company.getId());

    //3. 결과 조합후 반환
//    List<Dividend> dividends = new ArrayList<>();
//    for (var entity : dividendEntities) {
//
//      dividends.add(Dividend.builder()
//              .date(entity.getDate())
//              .dividend(entity.getDividend())
//              .build());
//    }

    List<Dividend> dividends = dividendEntities.stream()
                                                .map(e -> Dividend.builder()
                                                    .date(e.getDate())
                                                    .dividend(e.getDividend())
                                                    .build())
                                                .collect(Collectors.toList());

    return new ScrapedResult(Company.builder()
                                    .ticker(company.getTicker())
                                    .name(company.getName())
                                    .build(),
                            dividends);
  }

}
