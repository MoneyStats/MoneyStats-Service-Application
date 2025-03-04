package com.giova.service.moneystats.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.giova.service.moneystats.app.model.Dashboard;
import com.giova.service.moneystats.app.stats.StatsComponent;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.WalletService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.utilities.Utils;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import io.github.giovannilamarmora.utils.utilities.ObjectToolkit;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Logged
@Service
@AllArgsConstructor
public class AppService {

  private final UserData user;
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  @Autowired private WalletService walletService;
  @Autowired private StatsComponent statsComponent;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getDashboardData() {
    LOG.info("Starting getting data for dashboard for user {}", user.getUsername());
    List<LocalDate> getAllDates = statsComponent.getDistinctDates(user);
    List<LocalDate> filter;
    Dashboard dashboard = new Dashboard();
    if (!getAllDates.isEmpty()) {
      int currentYear = getAllDates.getLast().getYear();
      filter = getAllDates.stream().filter(d -> d.getYear() == currentYear).toList();

      Map<String, Dashboard> getData = mapDashBoard(filter, false);
      if (!ObjectToolkit.isNullOrEmpty(getData)) {
        dashboard = getData.get(String.valueOf(currentYear));
      }
    } else {
      List<Wallet> getAllWallet = new ArrayList<>();
      ResponseEntity<Response> responseEntityWallet =
          walletService.getAllWallets(null, true, false, false);
      if (!ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody())
          & !ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody().getData()))
        getAllWallet =
            Mapper.convertObject(
                responseEntityWallet.getBody().getData(), new TypeReference<List<Wallet>>() {});
      dashboard.setWallets(getAllWallet);
    }

    String message = "Data for Dashboard!";

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), dashboard);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getResumeData(Long year) {
    LOG.info("Starting getting resume data for dashboard for user {}", user.getUsername());
    List<LocalDate> getAllDates = statsComponent.getDistinctDates(user);
    Map<String, Dashboard> getData = new HashMap<>();
    Dashboard dashboard = new Dashboard();
    if (!getAllDates.isEmpty()) {
      List<LocalDate> filterDateByYear =
          getAllDates.stream().filter(localDate -> localDate.getYear() == year).toList();
      boolean hasPreviousYears =
          getAllDates.stream().anyMatch(localDate -> localDate.getYear() < year);
      LocalDate today = LocalDate.now();
      Map<String, Dashboard> getDataProvision =
          ObjectToolkit.isNullOrEmpty(filterDateByYear)
              ? null
              : mapDashBoard(filterDateByYear, (today.getYear() != year));
      if (!ObjectToolkit.isNullOrEmpty(getDataProvision)) {
        dashboard = getDataProvision.get(String.valueOf(year));
      }
      dashboard.setHasMoreRecords(hasPreviousYears); // Imposta il valore di hasMoreRecords
      dashboard.setYearsWalletStats(
          getAllDates.stream()
              .map(LocalDate::getYear)
              .distinct()
              .sorted(Collections.reverseOrder())
              .toList());
      getData.put(String.valueOf(year), dashboard);
    } else {
      List<Wallet> getAllWallet = Collections.emptyList();
      ResponseEntity<Response> responseEntityWallet =
          walletService.getAllWallets(null, true, false, false);
      if (!ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody())
          & !ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody().getData()))
        getAllWallet =
            Mapper.convertObject(
                responseEntityWallet.getBody().getData(), new TypeReference<List<Wallet>>() {});
      dashboard.setHasMoreRecords(false);
      dashboard.setWallets(getAllWallet);
      getData.put(String.valueOf(year), dashboard);
    }

    String message = "Data for Resume!";

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), getData);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> getHistoryData() {
    LOG.info("Starting getting history data for dashboard for user {}", user.getUsername());

    Map<String, Dashboard> dashboardHashMap = new HashMap<>();
    List<LocalDate> allDates = statsComponent.getDistinctDates(user);
    List<Integer> distinctYears =
        new ArrayList<>(
            allDates.stream()
                .map(LocalDate::getYear)
                .distinct()
                .sorted(Collections.reverseOrder())
                .toList());

    boolean isLiveWalletActive = Utils.isLiveWallet(null, user);

    if (isLiveWalletActive) {
      LocalDate today = LocalDate.now();
      if (!distinctYears.contains(today.getYear())) distinctYears.add(today.getYear());
      distinctYears.sort(Collections.reverseOrder());
    }

    ResponseEntity<Response> responseWallets =
        walletService.getAllWallets(null, true, false, false);
    List<Wallet> wallets =
        Optional.ofNullable(responseWallets.getBody())
            .map(Response::getData)
            .map(data -> Mapper.convertObject(data, new TypeReference<List<Wallet>>() {}))
            .orElse(Collections.emptyList());

    AtomicInteger yearIndex = new AtomicInteger(0);

    distinctYears.forEach(
        year -> {
          List<LocalDate> datesByYear = allDates.stream().filter(d -> d.getYear() == year).toList();
          Dashboard dashboard = new Dashboard();
          AtomicReference<Double> balance = new AtomicReference<>(0D);
          AtomicReference<Double> lastBalance = new AtomicReference<>(0D);

          wallets.forEach(
              wallet -> {
                List<Stats> yearlyStats =
                    Optional.ofNullable(wallet.getHistory())
                        .map(
                            history ->
                                history.stream()
                                    .filter(h -> h.getDate().getYear() == year)
                                    .toList())
                        .orElse(Collections.emptyList());

                Stats lastStats =
                    ObjectToolkit.isNullOrEmpty(wallet.getHistory())
                        ? null
                        : wallet.getHistory().getLast();

                if (yearIndex.get() == 0 && isLiveWalletActive) {
                  balance.updateAndGet(b -> b + wallet.getBalance());
                }

                if (!yearlyStats.isEmpty()) {
                  if (yearIndex.get() != 0 || !isLiveWalletActive) {
                    AppMapper.updateBalance(yearlyStats, datesByYear, balance);
                    AppMapper.updateLastBalance(yearlyStats, datesByYear, lastBalance);
                  } else {
                    AppMapper.updateBalance(yearlyStats, datesByYear, lastBalance);
                  }
                } else if (!ObjectToolkit.isNullOrEmpty(lastStats) && isLiveWalletActive) {
                  AppMapper.updateBalance(
                      List.of(lastStats), List.of(lastStats.getDate()), lastBalance);
                }
              });

          AppMapper.mapDashboardBalanceAndPerformance(dashboard, balance, lastBalance, null);
          dashboardHashMap.put(String.valueOf(year), dashboard);
          yearIndex.incrementAndGet();
        });

    String message = "Data for History!";
    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), dashboardHashMap);
    return ResponseEntity.ok(response);
  }

  private Map<String, Dashboard> mapDashBoard(List<LocalDate> dates, Boolean isResume) {
    Map<String, Dashboard> response = new HashMap<>();

    Integer distinctDatesByYear =
        dates.stream().map(LocalDate::getYear).distinct().toList().getFirst();

    // Wallet List
    ResponseEntity<Response> responseEntityWallet =
        walletService.getAllWallets(null, true, false, false);
    List<Wallet> getAllWallet = new ArrayList<>();
    if (!ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody())
        & !ObjectToolkit.isNullOrEmpty(responseEntityWallet.getBody().getData()))
      getAllWallet =
          Mapper.convertObject(
              responseEntityWallet.getBody().getData(), new TypeReference<List<Wallet>>() {});

    List<Wallet> finalGetAllWallet = getAllWallet;
    LOG.info("Mapping Data for year {}", distinctDatesByYear);
    // Filtro le date secondo l'anno
    List<LocalDate> filterDateByYear =
        dates.stream().filter(d -> d.getYear() == distinctDatesByYear).toList();
    Dashboard dashboard = new Dashboard();
    dashboard.setStatsWalletDays(filterDateByYear);
    dashboard.setPerformanceLastDate(filterDateByYear.getLast());
    dashboard.setPerformanceSince(filterDateByYear.getFirst());
    AtomicReference<Double> balance = new AtomicReference<>(0D);
    AtomicReference<Double> initialBalance = new AtomicReference<>(0D);
    AtomicReference<Double> lastBalance = new AtomicReference<>(0D);

    List<Wallet> filterWallet =
        new ArrayList<>(
            finalGetAllWallet.stream()
                .map(
                    wallet -> {
                      Wallet wallet1 = new Wallet();
                      BeanUtils.copyProperties(wallet, wallet1);
                      wallet1.setAssets(null);
                      List<Stats> listFilter =
                          !ObjectToolkit.isNullOrEmpty(wallet.getHistory())
                              ? wallet.getHistory().stream()
                                  .filter(h -> h.getDate().getYear() == distinctDatesByYear)
                                  .toList()
                              : Collections.emptyList();
                      wallet1.setHistory(listFilter);

                      if (!isResume && Utils.isLiveWallet(null, user))
                        balance.updateAndGet(b -> b + wallet.getBalance());

                      if (!listFilter.isEmpty()) {
                        if (isResume) {
                          AppMapper.updateBalance(listFilter, filterDateByYear, balance);
                          AppMapper.updateLastBalance(listFilter, filterDateByYear, lastBalance);
                        } else {
                          if (Utils.isLiveWallet(null, user))
                            // Uso questa funzione perchè prende l'ultimo saldo
                            AppMapper.updateBalance(listFilter, filterDateByYear, lastBalance);
                          else {
                            AppMapper.updateBalance(listFilter, filterDateByYear, balance);
                            AppMapper.updateLastBalance(listFilter, filterDateByYear, lastBalance);
                          }
                        }
                        AppMapper.updateInitialBalance(
                            listFilter, filterDateByYear, initialBalance);
                      }

                      checkAndMapWalletInThePast(isResume, listFilter, filterDateByYear, wallet1);
                      return wallet1;
                    })
                .toList());

    // Filtro Wallet cancellati da anni che non hanno stats
    Predicate<Wallet> walletRemovedInThePast =
        wallet -> wallet.getHistory().isEmpty() && wallet.getDeletedDate() != null;
    filterWallet.removeIf(walletRemovedInThePast);

    // Mi serve per mappare il passato
    if (isResume) {
      // Remove wallet that haven't any stats
      Predicate<Wallet> hasEmptyStats = wallet -> wallet.getHistory().isEmpty();
      filterWallet.removeIf(hasEmptyStats);
    }

    dashboard.setWallets(filterWallet);
    AppMapper.mapDashboardBalanceAndPerformance(dashboard, balance, lastBalance, initialBalance);
    response.put(String.valueOf(distinctDatesByYear), dashboard);

    return response;
  }

  private void checkAndMapWalletInThePast(
      Boolean isResume, List<Stats> listFilter, List<LocalDate> filterDateByYear, Wallet wallet1) {
    // Mi serve per mappare il passato
    if (isResume && !listFilter.isEmpty()) {
      // Se lo stats all'ultima posizione ha la stessa data
      // dell'ultima
      // data della lista dell'anno, il wallet non era ancora
      // cancellato
      if (listFilter.getLast().getDate().isEqual(filterDateByYear.getLast())) {
        wallet1.setDeletedDate(null);
      }
      AppMapper.mapWalletInThePast(wallet1);
    }
  }
}
