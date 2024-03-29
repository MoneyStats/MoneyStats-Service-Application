package com.giova.service.moneystats.app.stats;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.stats.entity.StatsEntity;
import com.giova.service.moneystats.app.wallet.entity.WalletEntity;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class StatsServiceTest {

  @Autowired private StatsService statsService;

  @MockBean private IStatsDAO iStatsDAO;

  @Test
  public void testGetDistinctDates() {
    UserEntity user = new UserEntity();
    user.setId(123L);

    LocalDate date1 = LocalDate.of(2022, 1, 1);
    LocalDate date2 = LocalDate.of(2022, 1, 2);
    List<LocalDate> dates = Arrays.asList(date1, date2);

    when(iStatsDAO.selectAppDistinctDate(user.getId())).thenReturn(dates);

    List<LocalDate> distinctDates = statsService.getDistinctDates(user);

    assertEquals(2, distinctDates.size());
    assertTrue(distinctDates.contains(date1));
    assertTrue(distinctDates.contains(date2));
  }

  @Test
  public void testGetStatsByWallet() {
    Long walletId = 123L;
    WalletEntity wallet = new WalletEntity();
    wallet.setId(walletId);

    List<StatsEntity> statsEntities = new ArrayList<>();
    StatsEntity statsEntity = new StatsEntity();
    statsEntity.setId(1L);
    statsEntity.setWallet(wallet);
    statsEntities.add(statsEntity);

    when(iStatsDAO.findStatsEntitiesByWalletId(walletId)).thenReturn(statsEntities);

    List<Stats> stats = statsService.getStatsByWallet(walletId);

    assertEquals(1, stats.size());
    assertEquals(1L, stats.get(0).getId());
  }
}
