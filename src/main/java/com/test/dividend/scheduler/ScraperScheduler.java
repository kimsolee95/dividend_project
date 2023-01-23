package com.test.dividend.scheduler;

import com.test.dividend.model.Company;
import com.test.dividend.model.ScrapedResult;
import com.test.dividend.model.constants.CacheKey;
import com.test.dividend.persist.entity.CompanyEntity;
import com.test.dividend.persist.entity.DividendEntity;
import com.test.dividend.persist.repository.CompanyRepository;
import com.test.dividend.persist.repository.DividendRepository;
import com.test.dividend.scraper.Scraper;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

  private final CompanyRepository companyRepository;
  private final DividendRepository dividendRepository;
  private final Scraper yahooFinanceScraper;

  //일정 주기마다 수행
  @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true) //스케줄러 동작 시 redis key finance에 해당하면 모두 비우기
  @Scheduled(cron = "${scheduler.scrap.yahoo}")
  public void yahooFinanceScheduling() {

    log.info("scraping scheduler is started");

    //저장된 회사 목록을 조회
    List<CompanyEntity> companies = this.companyRepository.findAll();

    //회사마다 배당금 정보를 새로 스크래핑
    for (var company : companies) {

      log.info("scraping scheduler is started ->" + company.getName());
      ScrapedResult scrapedResult = this.yahooFinanceScraper.scrap(
                                                  new Company(company.getTicker(), company.getName()));

      //스크래핑한 배당금 정보 중 데이터베이스에 없는 값 저장.
      scrapedResult.getDividends().stream()
          .map(e -> new DividendEntity(company.getId(), e)) //dividend model -> new dividend Entity
          .forEach(e -> { //new dividend Entity -> check and save
            boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
            if (!exists) {
              this.dividendRepository.save(e);
            }
          });

      //스크래핑 대상 사이트 서버 요청 term 주기
      try {
        Thread.sleep(3000); //3초
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }


    }

  }

}
