package com.giova.service.moneystats.app.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.app.category.dto.Category;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dashboard {

  private Double balance;
  private String value;
  private Double performance;
  private Double performanceValue;
  private LocalDate performanceSince;
  private LocalDate performanceLastDate;
  private Double lastStatsPerformance;
  private Double lastStatsBalanceDifference;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<LocalDate> statsWalletDays;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Category> categories;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Wallet> wallets;
}
