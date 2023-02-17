package com.giova.service.moneystats.app.stats;

import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface IStatsDAO extends JpaRepository<StatsEntity, Long> {

  /**
   * Used on {@link StatsEntity}, to get all the Date ordered and just one time
   *
   * @param userId
   * @return a List od unique and not duplicate date
   */
  @Query(
      value =
          "select distinct STATS.date from StatsEntity STATS where STATS.user.id = :userId order by STATS.date")
  List<LocalDate> selectDistinctDate(Long userId);

  List<StatsEntity> findStatsEntitiesByWalletId(Long walletId);
}
