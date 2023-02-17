package com.giova.service.moneystats.authentication;

import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.dto.UserRole;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.authentication.token.TokenService;
import com.giova.service.moneystats.authentication.token.dto.AuthToken;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.github.giovannilamarmora.utils.interceptors.correlationID.CorrelationIdUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Logged
@RequiredArgsConstructor
public class AuthService {

  @Value(value = "${app.invitationCode}")
  private String registerToken;

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  @Autowired private IAuthDAO iAuthDAO;

  @Autowired private AuthMapper authMapper;

  @Autowired private TokenService tokenService;

  final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

  private final UserEntity user;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> register(User user, String invitationCode) throws UtilsException {
    user.setRole(UserRole.USER);

    if (!registerToken.equalsIgnoreCase(invitationCode)) {
      LOG.error("Invitation code: {}, is wrong", invitationCode);
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_005, AuthException.ERR_AUTH_MSS_005.getMessage());
    }

    UserEntity userEntity = authMapper.mapUserToUserEntity(user);
    userEntity.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
    user.setPassword(null);

    UserEntity saved = iAuthDAO.save(userEntity);
    saved.setPassword(null);

    String message = "User: " + user.getUsername() + " Successfully registered!";

    Response response =
        new Response(
            HttpStatus.OK.value(),
            message,
            CorrelationIdUtils.getCorrelationId(),
            authMapper.mapUserEntityToUser(saved));

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> login(String username, String password) throws UtilsException {

    UserEntity userEntity = iAuthDAO.findUserEntityByUsername(username);
    if (userEntity == null) {
      LOG.error("User not found");
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_003, AuthException.ERR_AUTH_MSS_003.getMessage());
    }
    boolean matches = bCryptPasswordEncoder.matches(password, userEntity.getPassword());
    if (!matches) {
      LOG.error("User not found");
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_003, AuthException.ERR_AUTH_MSS_003.getMessage());
    }

    User user = authMapper.mapUserEntityToUser(userEntity);
    user.setPassword(null);
    user.setAuthToken(tokenService.generateToken(user));

    String message = "Login Successfully! Welcome back " + user.getUsername() + "!";

    Response response =
        new Response(HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), user);

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> checkLoginFE(String authToken) {

    User userDTO = authMapper.mapUserEntityToUser(user);
    userDTO.setPassword(null);

    String message = "Welcome back " + user.getUsername() + "!";

    Response response =
        new Response(
            HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), userDTO);

    return ResponseEntity.ok(response);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public UserEntity checkLogin(String authToken) throws UtilsException {
    AuthToken token = new AuthToken();
    token.setAccessToken(authToken);
    User user = new User();
    try {
      user = tokenService.parseToken(token);
    } catch (UtilsException e) {
      throw new UtilsException(AuthException.ERR_AUTH_MSS_004, e.getMessage());
    }
    UserEntity userEntity = iAuthDAO.findUserEntityByUsername(user.getUsername());
    // userEntity.setPassword(null);

    if (userEntity == null) {
      LOG.error("User not found");
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_003, AuthException.ERR_AUTH_MSS_003.getMessage());
    }
    return userEntity;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public AuthToken regenerateToken(UserEntity userEntity) throws UtilsException {
    User user = authMapper.mapUserEntityToUser(userEntity);
    AuthToken authToken = tokenService.generateToken(user);

    if (authToken == null) {
      LOG.error("Token not found");
      throw new UtilsException(
          AuthException.ERR_AUTH_MSS_003, AuthException.ERR_AUTH_MSS_003.getMessage());
    }
    return authToken;
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public ResponseEntity<Response> updateUserData(String authToken, User userToUpdate) {

    UserEntity userEntity = authMapper.mapUserToUserEntity(userToUpdate);
    if (userToUpdate.getPassword() != null && !userToUpdate.getPassword().isBlank()) {
      userEntity.setPassword(bCryptPasswordEncoder.encode(userToUpdate.getPassword()));
    } else {
      userEntity.setPassword(user.getPassword());
    }

    UserEntity saved = iAuthDAO.save(userEntity);
    saved.setPassword(null);

    String message = "User updated!";

    Response response =
        new Response(HttpStatus.OK.value(), message, CorrelationIdUtils.getCorrelationId(), saved);

    return ResponseEntity.ok(response);
  }
}
