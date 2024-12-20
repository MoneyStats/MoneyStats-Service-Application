package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.api.accessSphere.AccessSphereClient;
import com.giova.service.moneystats.api.accessSphere.dto.UserInfoResponse;
import com.giova.service.moneystats.api.emailSender.EmailSenderService;
import com.giova.service.moneystats.api.emailSender.dto.EmailContent;
import com.giova.service.moneystats.app.attachments.ImageService;
import com.giova.service.moneystats.app.attachments.dto.Image;
import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.dto.UserData;
import com.giova.service.moneystats.authentication.dto.UserRole;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.authentication.token.TokenService;
import com.giova.service.moneystats.authentication.token.dto.AuthToken;
import com.giova.service.moneystats.config.AppRole;
import com.giova.service.moneystats.exception.config.ExceptionMap;
import com.giova.service.moneystats.settings.entity.UserSettingEntity;
import com.giova.service.moneystats.utilities.RegEx;
import com.giova.service.moneystats.utilities.Utils;
import com.nimbusds.jose.JOSEException;
import io.github.giovannilamarmora.utils.context.TraceUtils;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.generic.Response;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.utilities.Mapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

@Service
@Logged
@RequiredArgsConstructor
public class AuthService {

  final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
  private final Logger LOG = LoggerFactory.getLogger(this.getClass());
  private final UserEntity user;

  @Value(value = "${app.invitationCode}")
  private String registerToken;

  @Value(value = "${app.fe.url}")
  private String feUrl;

  @Autowired private AuthCacheService authCacheService;
  @Autowired private AuthMapper authMapper;
  @Autowired private TokenService tokenService;
  @Autowired private EmailSenderService emailSenderService;
  @Autowired private ImageService imageService;
  @Autowired private AccessSphereClient accessSphereClient;

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<UserData> authorize(String access_token, String sessionId) {
    return accessSphereClient
        .getUserInfo(access_token, sessionId, true)
        .flatMap(
            responseEntity -> {
              if (responseEntity.getStatusCode().isError()
                  || responseEntity.getBody() == null
                  || responseEntity.getBody().getData() == null) {
                throw new AuthException(
                    ExceptionMap.ERR_AUTH_MSS_008, ExceptionMap.ERR_AUTH_MSS_008.getMessage());
              }
              UserInfoResponse userInfoResponse =
                  Mapper.convertObject(responseEntity.getBody().getData(), UserInfoResponse.class);
              if (userInfoResponse.getUserInfo().getRoles().stream()
                  .noneMatch(string -> Utils.isEnumValue(string, AppRole.class))) {
                LOG.error(
                    "The current user role {} not match with the app role",
                    userInfoResponse.getUserInfo().getRoles());
                throw new AuthException(
                    ExceptionMap.ERR_AUTH_MSS_010, ExceptionMap.ERR_AUTH_MSS_010.getMessage());
              }
              return Mono.just(
                  AuthMapper.mapAccessSphereUserToUserData(userInfoResponse.getUser()));
            });
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> register(User user, String invitationCode) throws UtilsException {
    user.setRole(UserRole.USER);
    // user.setPassword(new String(Base64.getDecoder().decode(user.getPassword())));

    if (!registerToken.equalsIgnoreCase(invitationCode)) {
      LOG.error("Invitation code: {}, is wrong", invitationCode);
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_005, ExceptionMap.ERR_AUTH_MSS_005.getMessage());
    }

    if (!Utils.checkCharacterAndRegexValid(user.getPassword(), RegEx.PASSWORD_FULL.getValue())) {
      LOG.error("Invalid regex for field password for user {}", user.getUsername());
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_009, "Invalid regex for field password");
    }

    if (!Utils.checkCharacterAndRegexValid(user.getEmail(), RegEx.EMAIL.getValue())) {
      LOG.error("Invalid regex for field email for user {}", user.getUsername());
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_009, "Invalid regex for field email");
    }

    UserEntity userEntity = authMapper.mapUserToUserEntity(user);
    userEntity.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    user.setPassword(null);

    UserEntity saved = authCacheService.save(userEntity);
    saved.setPassword(null);

    String message = "User: " + user.getUsername() + " Successfully registered!";

    Response response =
        new Response(
            HttpStatus.OK.value(),
            message,
            TraceUtils.getSpanID(),
            authMapper.mapUserEntityToUser(saved));

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> login(String basic) throws UtilsException, JOSEException {
    String username;
    String password;
    try {
      String[] decoded =
          new String(Base64.getDecoder().decode(basic.split("Basic ")[1])).split(":");
      username = decoded[0];
      password = decoded[1];
    } catch (Exception e) {
      LOG.error("Error during decoding password, message is {}", e.getMessage());
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_008, ExceptionMap.ERR_AUTH_MSS_008.getMessage());
    }

    LOG.debug("Login process started for user {}", username);
    // password = new String(Base64.getDecoder().decode(password));
    String email = username.contains("@") ? username : null;
    username = email != null ? null : username;

    UserEntity userEntity = authCacheService.findUserEntityByUsernameOrEmail(username, email);
    if (userEntity == null) {
      LOG.error("User not found");
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_002, ExceptionMap.ERR_AUTH_MSS_002.getMessage());
    }
    boolean matches = bCryptPasswordEncoder.matches(password, userEntity.getPassword());
    if (!matches) {
      LOG.error("User not found");
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_002, ExceptionMap.ERR_AUTH_MSS_002.getMessage());
    }

    migrateToSettings(userEntity);

    User user = authMapper.mapUserEntityToUser(userEntity);
    user.setPassword(null);
    user.setAuthToken(tokenService.generateToken(user));

    String message = "Login Successfully! Welcome back " + user.getUsername() + "!";

    Response response = new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), user);
    LOG.debug("Login process ended for user {}", username);
    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public Mono<ResponseEntity<Response>> forgotPassword(String email) throws UtilsException {
    if (!Utils.checkCharacterAndRegexValid(email, RegEx.EMAIL.getValue())) {
      LOG.error("Invalid regex for field email");
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_009, "Invalid regex for field email");
    }
    UserEntity userEntity = authCacheService.findUserEntityByEmail(email);
    if (userEntity == null) {
      LOG.error("User not found");
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_006, ExceptionMap.ERR_AUTH_MSS_006.getMessage());
    }
    String token = UUID.randomUUID().toString();
    userEntity.setTokenReset(token);
    authCacheService.save(userEntity);

    // Send Email
    EmailContent emailContent =
        EmailContent.builder()
            .subject("MoneyStats - Reset your Password")
            .to(email)
            .sentDate(new Date())
            .build();
    Map<String, String> param = new HashMap<>();
    param.put("{{RESET_URL}}", feUrl + "/auth/resetPassword/token/" + token);
    return emailSenderService
        .sendEmail(EmailContent.RESET_TEMPLATE, param, emailContent)
        .flatMap(
            emailResponse -> {
              emailResponse.setToken(token);

              String message = "Email Sent! Check your email address!";

              Response response =
                  new Response(
                      HttpStatus.OK.value(), message, TraceUtils.getSpanID(), emailResponse);

              return Mono.just(ResponseEntity.ok(response));
            });
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> resetPassword(String password, String token)
      throws UtilsException {
    password = new String(Base64.getDecoder().decode(password));
    if (!Utils.checkCharacterAndRegexValid(password, RegEx.PASSWORD_FULL.getValue())) {
      LOG.error("Invalid regex for field password");
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_009, "Invalid regex for field password");
    }

    UserEntity userEntity = authCacheService.findUserEntityByTokenReset(token);
    if (userEntity == null) {
      LOG.error("User not found");
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_008, ExceptionMap.ERR_AUTH_MSS_008.getMessage());
    }
    if (userEntity.getUpdateDate().plusDays(1).isBefore(LocalDateTime.now())) {
      LOG.error("Token Expired");
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_008, ExceptionMap.ERR_AUTH_MSS_008.getMessage());
    }
    userEntity.setPassword(bCryptPasswordEncoder.encode(password));
    userEntity.setTokenReset(null);

    UserEntity saved = authCacheService.save(userEntity);
    userEntity.setPassword(null);

    String message = "Password Updated!";

    Response response =
        new Response(
            HttpStatus.OK.value(),
            message,
            TraceUtils.getSpanID(),
            authMapper.mapUserEntityToUser(saved));

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> userInfo(String authToken) throws JOSEException {

    User userDTO = authMapper.mapUserEntityToUser(user);
    userDTO.setPassword(null);
    userDTO.setAuthToken(regenerateToken(user));

    String message = "Obtained data for user " + user.getUsername() + "!";

    return ResponseEntity.ok(
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), userDTO));
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public UserEntity checkLogin(String authToken) throws UtilsException {
    AuthToken token = new AuthToken();
    token.setAccessToken(authToken);
    User user = new User();
    try {
      user = tokenService.parseToken(token);
    } catch (UtilsException e) {
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_008, e.getMessage());
    }
    UserEntity userEntity =
        authCacheService.findUserEntityByUsernameOrEmail(user.getUsername(), user.getEmail());
    // userEntity.setPassword(null);

    if (userEntity == null) {
      LOG.error("User not found");
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_002, ExceptionMap.ERR_AUTH_MSS_002.getMessage());
    }
    return userEntity;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public AuthToken regenerateToken(UserEntity userEntity) throws UtilsException, JOSEException {
    User user = authMapper.mapUserEntityToUser(userEntity);
    AuthToken authToken = tokenService.generateToken(user);

    if (authToken == null) {
      LOG.error("Token not found");
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_008, ExceptionMap.ERR_AUTH_MSS_008.getMessage());
    }
    return authToken;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> refreshToken(String authToken)
      throws UtilsException, JOSEException {
    AuthToken token = new AuthToken();
    token.setAccessToken(authToken.contains("Bearer") ? authToken : "Bearer " + authToken);
    User user = new User();
    try {
      user = tokenService.parseToken(token);
    } catch (UtilsException e) {
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_008, e.getMessage());
    }
    UserEntity userEntity =
        authCacheService.findUserEntityByUsernameOrEmail(user.getUsername(), user.getEmail());
    // userEntity.setPassword(null);

    if (ObjectUtils.isEmpty(userEntity)) {
      LOG.error("User not found");
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_008, ExceptionMap.ERR_AUTH_MSS_008.getMessage());
    }

    AuthToken refreshToken = tokenService.generateToken(user);

    if (ObjectUtils.isEmpty(refreshToken)) {
      LOG.error("Error on Refresh Token, not found");
      throw new AuthException(
          ExceptionMap.ERR_AUTH_MSS_008, ExceptionMap.ERR_AUTH_MSS_008.getMessage());
    }
    String message =
        "Token Refreshed, valid ultil "
            + LocalDateTime.ofInstant(
                Instant.ofEpochMilli(refreshToken.getExpirationTime()), ZoneId.systemDefault());

    Response response =
        new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), refreshToken);

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public ResponseEntity<Response> updateUserData(String authToken, User userToUpdate) {
    if (!Utils.checkCharacterAndRegexValid(userToUpdate.getEmail(), RegEx.EMAIL.getValue())) {
      LOG.error("Invalid regex for field email for user {}", userToUpdate.getUsername());
      throw new AuthException(ExceptionMap.ERR_AUTH_MSS_009, "Invalid regex for field email");
    }

    UserEntity userEntity = authMapper.mapUserToUserEntity(userToUpdate);
    if (userToUpdate.getPassword() != null && !userToUpdate.getPassword().isBlank()) {
      userToUpdate.setPassword(new String(Base64.getDecoder().decode(userToUpdate.getPassword())));
      if (!Utils.checkCharacterAndRegexValid(user.getPassword(), RegEx.PASSWORD_FULL.getValue())) {
        LOG.error("Invalid regex for field password for user {}", userToUpdate.getUsername());
        throw new AuthException(ExceptionMap.ERR_AUTH_MSS_009, "Invalid regex for field password");
      }
      userEntity.setPassword(bCryptPasswordEncoder.encode(userToUpdate.getPassword()));
    } else {
      userEntity.setPassword(user.getPassword());
    }

    if (userToUpdate.getImgName() != null && !userToUpdate.getImgName().isEmpty()) {
      LOG.info("Building attachment with filename {}", userToUpdate.getImgName());
      Image image = imageService.getAttachment(userToUpdate.getImgName());
      imageService.removeAttachment(userToUpdate.getImgName());
      userEntity.setProfilePhoto(
          "data:"
              + image.getContentType()
              + ";base64,"
              + Base64.getEncoder().encodeToString(image.getBody()));
    }

    UserEntity saved = authCacheService.save(userEntity);
    saved.setPassword(null);

    String message = "User updated!";

    Response response = new Response(HttpStatus.OK.value(), message, TraceUtils.getSpanID(), saved);

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.SERVICE)
  public List<String> getCryptoFiatUsersCurrency() {
    LOG.info("Getting Crypto Fiat Currency");
    return authCacheService.selectDistinctCryptoFiatCurrency();
  }

  private void migrateToSettings(UserEntity user) {
    LOG.info("Checking User {} to migrate", user.getUsername());
    if (user.getCurrency() != null || user.getSettings() == null) {
      LOG.info("Migrating user {} to the new settings", user.getUsername());
      UserSettingEntity userSettingEntity = new UserSettingEntity();
      userSettingEntity.setCurrency(user.getCurrency());
      user.setCurrency(null);
      userSettingEntity.setCryptoCurrency(user.getCryptoCurrency());
      user.setCryptoCurrency(null);
      user.setGithubUser(null);
      userSettingEntity.setUser(user);
      user.setSettings(userSettingEntity);
      authCacheService.save(user);
    }
    LOG.info("User {} not need migration", user.getUsername());
  }
}
