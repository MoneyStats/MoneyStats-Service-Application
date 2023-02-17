package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.authentication.dto.User;
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

@Logged
@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

  @Autowired private AuthService authService;

  @PostMapping(
      value = "/sign-up",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Authentication", description = "API to register an account")
  @Operation(description = "API to register an account", tags = "Authentication")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> signUp(
      @RequestBody @Valid User user, @RequestParam String invitationCode) throws UtilsException {
    return authService.register(user, invitationCode);
  }

  @PostMapping(
      value = "/login",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Authentication", description = "API to register an account")
  @Operation(description = "API to register an account", tags = "Authentication")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> login(
      @RequestParam String username, @RequestParam String password) throws UtilsException {
    return authService.login(username, password);
  }

  @GetMapping(value = "/check-login", produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Authentication", description = "API to check an account")
  @Operation(description = "API to check an account", tags = "Authentication")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> checkLogin(@RequestHeader("authToken") String authToken)
      throws UtilsException {
    return authService.checkLoginFE(authToken);
  }

  @PostMapping(
      value = "/update/user",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Tag(name = "Authentication", description = "API to update an account")
  @Operation(description = "API to update an account", tags = "Authentication")
  @LogInterceptor(type = LogTimeTracker.ActionType.APP_CONTROLLER)
  public ResponseEntity<Response> updateUser(
      @RequestHeader("authToken") String authToken, @RequestBody @Valid User user) {
    return authService.updateUserData(authToken, user);
  }
}
