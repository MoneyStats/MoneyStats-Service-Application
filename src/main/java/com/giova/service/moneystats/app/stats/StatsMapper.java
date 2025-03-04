package com.giova.service.moneystats.app.stats;

import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.dto.UserData;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.util.List;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class StatsMapper {

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<Stats> fromEntityToStats(List<StatsEntity> statsEntities) {
    if (ObjectToolkit.isNullOrEmpty(statsEntities)) return null;
    return statsEntities.stream()
        .map(
            statsEntity -> {
              Stats stats2 = new Stats();
              BeanUtils.copyProperties(statsEntity, stats2);
              return stats2;
            })
        .toList();
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.MAPPER)
  public static List<StatsEntity> fromStatsToEntity(
      List<Stats> stats, WalletEntity wallet, UserData user) {
    if (ObjectToolkit.isNullOrEmpty(stats)) return null;
    return stats.stream()
        .map(
            stats1 -> {
              StatsEntity statsEntity = new StatsEntity();
              BeanUtils.copyProperties(stats1, statsEntity);
              statsEntity.setWallet(wallet);
              statsEntity.setUserIdentifier(user.getIdentifier());
              return statsEntity;
            })
        .toList();
  }
}
