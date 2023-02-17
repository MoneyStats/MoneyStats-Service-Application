package com.giova.service.moneystats.app;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.giova.service.moneystats.app.model.GithubIssues;
import com.giova.service.moneystats.app.wallet.dto.Wallet;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Logged
@RestController
@RequestMapping("/v1/app")
@CrossOrigin(origins = "*")
public class AppController {

  @Autowired private AppService appService;

  @GetMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "App", description = "API to register an account")
  @Operation(description = "API to register an account", tags = "App")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> getDashboard(@RequestHeader("authToken") String authToken)
      throws UtilsException, JsonProcessingException {
    return appService.getDashboardData(authToken);
  }

  @GetMapping(value = "/resume", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "App", description = "API to register an account")
  @Operation(description = "API to register an account", tags = "App")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> getResume(@RequestHeader("authToken") String authToken)
      throws UtilsException {
    return appService.getResumeData(authToken);
  }

  @PostMapping(
      value = "/add/stats",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "App", description = "API to register an account")
  @Operation(description = "API to register an account", tags = "App")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> addStats(
      @RequestBody @Valid List<Wallet> wallets, @RequestHeader("authToken") String authToken)
      throws UtilsException {
    return appService.addStats(wallets, authToken);
  }

  @PostMapping(
      value = "/report/bug",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "App", description = "API to report a bug")
  @Operation(description = "API to report a bug", tags = "App")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> bugReport( // @RequestHeader("authToken") String authToken,
      @RequestBody @Valid GithubIssues githubIssues) throws JsonProcessingException {
    return appService.reportBug(githubIssues);
  }
}
