package com.giova.service.moneystats.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.app.category.CategoryService;
import com.giova.service.moneystats.app.category.dto.Category;
import com.giova.service.moneystats.api.github.GithubClient;
import com.giova.service.moneystats.app.model.Dashboard;
import com.giova.service.moneystats.app.model.GithubIssues;
import com.giova.service.moneystats.app.stats.StatsService;
import com.giova.service.moneystats.app.stats.dto.Stats;
import com.giova.service.moneystats.app.wallet.WalletService;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import io.github.giovannilamarmora.utils.math.MathService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Logged
@Service
@AllArgsConstructor
public class AppService {

  @Autowired private WalletService walletService;

  @Autowired private CategoryService categoryService;

  @Autowired private StatsService statsService;

  private final UserEntity user;

  @Autowired private GithubClient githubClient;

  private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> reportBug(GithubIssues githubIssues)
      throws JsonProcessingException {
    LOG.info("Bug to report: {}", objectMapper.writeValueAsString(githubIssues));

    ResponseEntity<Object> issues = githubClient.openGithubIssues(githubIssues);
    String message = "Bug Reported!";

    Response response =
        new Response(
            HttpStatus.OK.value(),
            message,
            CorrelationIdUtils.getCorrelationId(),
            issues.getBody());
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> getDashboardData(String authToken) throws UtilsException {

    List<LocalDate> getAllDates = statsService.getDistinctDates(user);
    List<LocalDate> filter = new ArrayList<>();
    Map<String, Dashboard> getData = new HashMap<>();
    int thisYear = 0;
    if (!getAllDates.isEmpty()) {
      int currentYear = getAllDates.get(getAllDates.size() - 1).getYear();
      filter =
          getAllDates.stream().filter(d -> d.getYear() == currentYear).collect(Collectors.toList());

      thisYear = currentYear;
      getData = mapDashBoard(filter, authToken);
    } else {
      Dashboard dashboard = new Dashboard();
      List<Category> getAllCategory =
          objectMapper.convertValue(
              categoryService.getAllCategories().getBody().getData(),
              new TypeReference<List<Category>>() {});
      List<Wallet> getAllWallet =
          objectMapper.convertValue(
              walletService.getWallets(authToken).getBody().getData(),
              new TypeReference<List<Wallet>>() {});
      dashboard.setWallets(getAllWallet);
      dashboard.setCategories(getAllCategory);
      getData.put(String.valueOf(thisYear), dashboard);
    }

    String message = "Data for Dashboard!";

    Response response =
        new Response(
            HttpStatus.OK.value(),
            message,
            CorrelationIdUtils.getCorrelationId(),
            getData.get(String.valueOf(thisYear)));
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> getResumeData(String authToken) throws UtilsException {

    List<LocalDate> getAllDates = statsService.getDistinctDates(user);
    Map<String, Dashboard> getData = new HashMap<>();
    int thisYear = LocalDate.now().getYear();
    if (!getAllDates.isEmpty()) {
      getData = mapDashBoard(getAllDates, authToken);
    } else {
      Dashboard dashboard = new Dashboard();
      List<Category> getAllCategory =
          objectMapper.convertValue(
              categoryService.getAllCategories().getBody().getData(),
              new TypeReference<List<Category>>() {});
      List<Wallet> getAllWallet =
          objectMapper.convertValue(
              walletService.getWallets(authToken).getBody().getData(),
              new TypeReference<List<Wallet>>() {});
      dashboard.setWallets(getAllWallet);
      dashboard.setCategories(getAllCategory);
      getData.put(String.valueOf(thisYear), dashboard);
    }

    String message = "Data for Resume!";

    Response response =
        new Response(
            HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), getData);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> addStats(List<Wallet> wallets, String authToken) {

    wallets.stream()
        .peek(
            wallet -> {
              try {
                objectMapper.convertValue(
                    walletService.insertOrUpdateWallet(wallet, authToken).getBody().getData(),
                    Wallet.class);
              } catch (UtilsException e) {
                throw new RuntimeException(e);
              }
              List<Stats> statsList = statsService.getStatsByWallet(wallet.getId());
              wallet.setHistory(statsList);
            })
        .collect(Collectors.toList());

    String message = "Stats Added Successfully!";

    Response response =
        new Response(
            HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), wallets);
    return ResponseEntity.ok(response);
  }

  private Map<String, Dashboard> mapDashBoard(List<LocalDate> dates, String authToken)
      throws UtilsException {
    Map<String, Dashboard> response = new HashMap<>();

    // Category List
    List<Category> getAllCategory =
        objectMapper.convertValue(
            categoryService.getAllCategories().getBody().getData(),
            new TypeReference<List<Category>>() {});

    List<Integer> distinctDatesByYear =
        dates.stream().map(LocalDate::getYear).distinct().collect(Collectors.toList());

    // Wallet List
    List<Wallet> getAllWallet =
        objectMapper.convertValue(
            walletService.getWallets(authToken).getBody().getData(),
            new TypeReference<List<Wallet>>() {});

    AtomicInteger index = new AtomicInteger(0);
    distinctDatesByYear.stream()
        .sorted(Collections.reverseOrder())
        .peek(
            year -> {
              LOG.info("Mapping Data for year {}", year);
              // Filtro le date secondo l'anno
              List<LocalDate> filterDateByYear =
                  dates.stream().filter(d -> d.getYear() == year).collect(Collectors.toList());
              Dashboard dashboard = new Dashboard();
              dashboard.setCategories(getAllCategory);
              dashboard.setStatsWalletDays(filterDateByYear);
              dashboard.setPerformanceLastDate(filterDateByYear.get(filterDateByYear.size() - 1));
              dashboard.setPerformanceSince(filterDateByYear.get(0));
              AtomicReference<Double> balance = new AtomicReference<>(0D);
              AtomicReference<Double> initialBalance = new AtomicReference<>(0D);
              AtomicReference<Double> lastBalance = new AtomicReference<>(0D);
              AtomicInteger indexWallet = new AtomicInteger(0);
              List<Wallet> filterWallet =
                  getAllWallet.stream()
                      .map(
                          wallet -> {
                            Wallet wallet1 = new Wallet();
                            BeanUtils.copyProperties(wallet, wallet1);
                            List<Stats> listFilter =
                                wallet.getHistory().stream()
                                    .filter(h -> h.getDate().getYear() == year)
                                    .collect(Collectors.toList());
                            wallet1.setHistory(listFilter);

                            if (!listFilter.isEmpty()) {
                              Double balanceFilter =
                                  listFilter.stream()
                                              .filter(
                                                  lF ->
                                                      lF.getDate()
                                                          .isEqual(
                                                              filterDateByYear.get(
                                                                  filterDateByYear.size() - 1)))
                                              .collect(Collectors.toList())
                                              .size()
                                          != 0
                                      ? listFilter.stream()
                                          .filter(
                                              lF ->
                                                  lF.getDate()
                                                      .isEqual(
                                                          filterDateByYear.get(
                                                              filterDateByYear.size() - 1)))
                                          .collect(Collectors.toList())
                                          .get(0)
                                          .getBalance()
                                      : 0;
                              balance.updateAndGet(v -> v + balanceFilter);
                              // balance.updateAndGet(v -> v + listFilter.get(listFilter.size() -
                              // 1).getBalance());
                              Double initialBalanceFilter =
                                  listFilter.stream()
                                              .filter(
                                                  lF ->
                                                      lF.getDate().isEqual(filterDateByYear.get(0)))
                                              .collect(Collectors.toList())
                                              .size()
                                          != 0
                                      ? listFilter.stream()
                                          .filter(
                                              lF -> lF.getDate().isEqual(filterDateByYear.get(0)))
                                          .collect(Collectors.toList())
                                          .get(0)
                                          .getBalance()
                                      : 0;
                              initialBalance.updateAndGet(v -> v + initialBalanceFilter);
                              // initialBalance.updateAndGet(v -> v +
                              // listFilter.get(0).getBalance());
                              // if (listFilter.size() > 1) {
                              Double lastBalanceFilter =
                                  listFilter.stream()
                                              .filter(
                                                  lF ->
                                                      lF.getDate()
                                                          .isEqual(
                                                              filterDateByYear.get(
                                                                  filterDateByYear.size() > 1
                                                                      ? filterDateByYear.size() - 2
                                                                      : 0)))
                                              .collect(Collectors.toList())
                                              .size()
                                          != 0
                                      ? listFilter.stream()
                                          .filter(
                                              lF ->
                                                  lF.getDate()
                                                      .isEqual(
                                                          filterDateByYear.get(
                                                              filterDateByYear.size() > 1
                                                                  ? filterDateByYear.size() - 2
                                                                  : 0)))
                                          .collect(Collectors.toList())
                                          .get(0)
                                          .getBalance()
                                      : 0;
                              // Double lastBalanceFilter = listFilter.stream().filter(lF ->
                              // lF.getDate().isEqual(filterDateByYear.get(filterDateByYear.size() -
                              // 2))).collect(Collectors.toList()).size() != 0 ?
                              //        listFilter.stream().filter(lF ->
                              // lF.getDate().isEqual(filterDateByYear.get(filterDateByYear.size() -
                              // 2))).collect(Collectors.toList()).get(0).getBalance() : 0;
                              lastBalance.updateAndGet(v -> v + lastBalanceFilter);
                              // lastBalance.updateAndGet(v -> v + listFilter.get(listFilter.size()
                              // - 2).getBalance());
                              // } else lastBalance.updateAndGet(v -> v + 0.01D);
                            }

                            // Mi serve per mappare il passato
                            if (index.get() > 0 && !listFilter.isEmpty()) {
                              // Se gli stats all'ultima posizione ha la stessa data dell'ultima
                              // data della lista dell'anno, il wallet non era cancellato
                              if (listFilter
                                  .get(listFilter.size() - 1)
                                  .getDate()
                                  .isEqual(filterDateByYear.get(filterDateByYear.size() - 1))) {
                                wallet1.setDeletedDate(null);
                              }
                              try {
                                mapWalletInThePast(wallet1);
                              } catch (UtilsException e) {
                                throw new RuntimeException(e);
                              }
                            }
                            indexWallet.incrementAndGet();
                            return wallet1;
                          })
                      .collect(Collectors.toList());

              // Filtro Wallet cancellati da anni che non hanno stats
              Predicate<Wallet> walletRemovedInThePast =
                  wallet -> wallet.getHistory().isEmpty() && wallet.getDeletedDate() != null;
              filterWallet.removeIf(walletRemovedInThePast);

              // Mi serve per mappare il passato
              if (index.get() > 0) {
                // Remove wallet that haven't any stats
                Predicate<Wallet> hasEmptyStats = wallet -> wallet.getHistory().isEmpty();
                filterWallet.removeIf(hasEmptyStats);
              }

              dashboard.setWallets(filterWallet);
              try {
                dashboard.setPerformanceValue(
                    MathService.round(balance.get() - initialBalance.get(), 2));
                dashboard.setLastStatsBalanceDifference(
                    MathService.round(balance.get() - lastBalance.get(), 2));
                dashboard.setBalance(MathService.round(balance.get(), 2));
                dashboard.setPerformance(
                    balance.get() == 0 && initialBalance.get() == 0
                        ? 0D
                        : MathService.round(
                            ((balance.get() - initialBalance.get()) / initialBalance.get()) * 100,
                            2));
                dashboard.setLastStatsPerformance(
                    balance.get() == 0 && lastBalance.get() == 0
                        ? 0D
                        : MathService.round(
                            ((balance.get() - lastBalance.get()) / lastBalance.get()) * 100, 2));
              } catch (UtilsException e) {
                throw new RuntimeException(e);
              }
              index.incrementAndGet();
              response.put(String.valueOf(year), dashboard);
            })
        .collect(Collectors.toList());

    return response;
  }

  private void mapWalletInThePast(Wallet wallet) throws UtilsException, RuntimeException {
    AtomicReference<Double> balance = new AtomicReference<>(0D);
    AtomicReference<Double> initialBalance = new AtomicReference<>(0D);
    AtomicReference<Double> lastBalance = new AtomicReference<>(0.00001D);
    Stats highPrice =
        wallet.getHistory().stream()
            .max(Comparator.comparing(Stats::getBalance))
            .orElseThrow(UtilsException::new);
    Stats lowPrice =
        wallet.getHistory().stream()
            .min(Comparator.comparing(Stats::getBalance))
            .orElseThrow(UtilsException::new);
    List<Stats> getStats = wallet.getHistory();
    wallet.setHighPrice(highPrice.getBalance());
    wallet.setHighPriceDate(highPrice.getDate());
    wallet.setLowPrice(lowPrice.getBalance());
    wallet.setLowPriceDate(lowPrice.getDate());

    balance.updateAndGet(
        v -> v + getStats.get(getStats.size() > 1 ? getStats.size() - 1 : 0).getBalance());
    lastBalance.updateAndGet(
        v -> v + getStats.get(getStats.size() > 1 ? getStats.size() - 2 : 0).getBalance());
    wallet.setDateLastStats(getStats.get(getStats.size() - 1).getDate());
    wallet.setDifferenceLastStats(MathService.round(balance.get() - lastBalance.get(), 2));
    wallet.setBalance(MathService.round(balance.get(), 2));

    wallet.setPerformanceLastStats(
        MathService.round(((balance.get() - lastBalance.get()) / lastBalance.get()) * 100, 2));
  }
}
