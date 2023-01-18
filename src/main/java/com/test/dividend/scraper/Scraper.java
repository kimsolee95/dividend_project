package com.test.dividend.scraper;

import com.test.dividend.model.Company;
import com.test.dividend.model.ScrapedResult;

public interface Scraper {

  Company scrapCompanyByTicker(String ticker);
  ScrapedResult scrap(Company company);
}
