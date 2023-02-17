package com.giova.service.moneystats.app.stats;

import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Logged
@Service
public class StatsService {

  @Autowired private IStatsDAO iStatsDAO;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<LocalDate> getDistinctDates(UserEntity user) {
    return iStatsDAO.selectDistinctDate(user.getId());
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<Stats> saveStats(List<Stats> stats, WalletEntity wallet, UserEntity user) {
    List<StatsEntity> statsEntities =
        stats.stream()
            .map(
                stats1 -> {
                  StatsEntity statsEntity = new StatsEntity();
                  BeanUtils.copyProperties(stats1, statsEntity);
                  statsEntity.setWallet(wallet);
                  statsEntity.setUser(user);
                  return statsEntity;
                })
            .collect(Collectors.toList());

    List<StatsEntity> saved = iStatsDAO.saveAll(statsEntities);

    List<Stats> response =
        saved.stream()
            .map(
                statsEntity -> {
                  Stats stats2 = new Stats();
                  BeanUtils.copyProperties(statsEntity, stats2);
                  return stats2;
                })
            .collect(Collectors.toList());
    return response;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public List<Stats> getStatsByWallet(Long walletId) {

    List<StatsEntity> stats = iStatsDAO.findStatsEntitiesByWalletId(walletId);
    List<Stats> response = new ArrayList<>();
    if (!stats.isEmpty()) {
      response =
          stats.stream()
              .map(
                  statsEntity -> {
                    Stats stats2 = new Stats();
                    BeanUtils.copyProperties(statsEntity, stats2);
                    return stats2;
                  })
              .collect(Collectors.toList());
    }

    return response;
  }
}
