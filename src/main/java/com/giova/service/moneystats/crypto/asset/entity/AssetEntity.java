package com.giova.service.moneystats.crypto.asset.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.generic.GenericEntity;
import java.time.LocalDate;
import java.util.List;
import javax.persistence.*;
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
@Table(name = "ASSETS")
public class AssetEntity extends GenericEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "IDENTIFIER")
  private String identifier;

  @Column(name = "NAME")
  private String name;

  @Column(name = "SYMBOL")
  private String symbol;

  @Column(name = "RANK")
  private Long rank;

  @Column(name = "ICON")
  private String icon;

  @Column(name = "BALANCE")
  private Double balance;

  @Column(name = "INVESTED")
  private Double invested;

  @Column(name = "LAST_UPDATE")
  private LocalDate lastUpdate;

  @Column(name = "PERFORMANCE")
  private Double performance;

  @Column(name = "TREND")
  private Double trend;

  @ManyToOne
  @JoinColumn(name = "USER_ID", nullable = false)
  private UserEntity user;

  @ManyToOne
  @JoinColumn(name = "WALLET_ID", nullable = false)
  private WalletEntity wallet;

  @OrderBy(value = "date")
  @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL)
  private List<StatsEntity> history;
}
