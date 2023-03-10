package com.test.dividend.persist.entity;

import com.test.dividend.model.Company;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "COMPANY")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class CompanyEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String ticker;

  private String name;

  public CompanyEntity(Company company) {
    this.ticker = company.getTicker();
    this.name = company.getName();
  }

}
