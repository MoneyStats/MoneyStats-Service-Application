package com.giova.service.moneystats.app.wallet.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.crypto.asset.entity.AssetEntity;
import io.github.giovannilamarmora.utils.generic.GenericEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "WALLET")
public class WalletEntity extends GenericEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "CATEGORY", nullable = false)
  private String category;

  @Column(name = "TYPE")
  private String type;

  @Column(name = "BALANCE", nullable = false)
  private Double balance;

  @Lob
  @Column(name = "IMG", columnDefinition = "LONGTEXT", nullable = false)
  private String img;

  @Column(name = "ALL_TIME_HIGH", nullable = false)
  private Double allTimeHigh;

  @Column(name = "ALL_TIME_HIGH_DATE", nullable = false)
  private LocalDate allTimeHighDate;

  @Column(name = "HIGH_PRICE", nullable = false)
  private Double highPrice;

  @Column(name = "HIGH_PRICE_DATE", nullable = false)
  private LocalDate highPriceDate;

  @Column(name = "LOW_PRICE", nullable = false)
  private Double lowPrice;

  @Column(name = "LOW_PRICE_DATE", nullable = false)
  private LocalDate lowPriceDate;

  @Column(name = "PERFORMANCE_LAST_STATS", nullable = false)
  private Double performanceLastStats;

  @Column(name = "DIFFERENCE_LAST_STATS", nullable = false)
  private Double differenceLastStats;

  @Column(name = "DATE_LAST_STATS")
  private LocalDate dateLastStats;

  @Lob
  @Column(name = "INFO")
  private String info;

  @Column(name = "USER_IDENTIFIER", nullable = false)
  private String userIdentifier;

  @OrderBy(value = "rank")
  @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<AssetEntity> assets;

  @OrderBy(value = "date")
  @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<StatsEntity> history;

  public WalletEntity empty() {
    return null;
  }
}
