package com.giova.service.moneystats.scheduler;

import com.giova.service.moneystats.app.wallet.WalletCacheService;
import com.giova.service.moneystats.authentication.AuthCacheService;
import com.giova.service.moneystats.crypto.coinGecko.MarketDataCacheService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CronCachingReset {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Value(value = "#{new Boolean(${rest.scheduled.caching.active:false})}")
  @Autowired
  private Boolean isSchedulerActive;

  @Autowired private MarketDataCacheService marketDataCacheService;
  @Autowired private AuthCacheService authCacheService;
  @Autowired private WalletCacheService walletCacheService;

  @Scheduled(cron = "${rest.scheduled.caching.cron}")
  void scheduleCleanCache() {
    LOG.info("[Clean-Cache] Scheduler Started at {}", LocalDateTime.now());

    if (!isSchedulerActive) {
      LOG.info(
          "[Clean-Cache] Scheduler Active status is {}, Stopping Scheduler", isSchedulerActive);
      return;
    }

    walletCacheService.deleteWalletsCache();
    authCacheService.deleteUserCache();
    marketDataCacheService.deleteAllMarketDataCache();

    LOG.info("[Clean-Cache] Scheduler Finished at {}", LocalDateTime.now());
  }
}