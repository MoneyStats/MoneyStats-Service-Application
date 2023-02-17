package com.giova.service.moneystats.authentication.token;

import com.giova.service.moneystats.authentication.AuthException;
import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.dto.UserRole;
import com.giova.service.moneystats.authentication.token.dto.AuthToken;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.interceptors.LogInterceptor;
import io.github.giovannilamarmora.utils.interceptors.LogTimeTracker;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Logged
@Service
public class TokenService {

  private final Logger LOG = LoggerFactory.getLogger(this.getClass());

  private static final String FIRSTNAME = "firstName";
  private static final String LASTNAME = "lastName";
  private static final String PROFILE_PHOTO = "profilePhoto";
  private static final String CURRENCY = "currency";
  private static final String EMAIL = "email";
  private static final String ROLE = "role";

  @Value(value = "${jwt.secret}")
  private String secret;

  @Value(value = "${jwt.time}")
  private String expirationTime;

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public AuthToken generateToken(User user) {
    Claims claims = Jwts.claims().setSubject(user.getUsername());
    claims.put(FIRSTNAME, user.getName());
    claims.put(LASTNAME, user.getSurname());
    claims.put(EMAIL, user.getEmail());
    claims.put(ROLE, user.getRole());
    claims.put(PROFILE_PHOTO, user.getProfilePhoto());
    claims.put(CURRENCY, user.getCurrency());
    long dateExp = Long.parseLong(expirationTime);
    Date exp = new Date(System.currentTimeMillis() + dateExp);
    String token =
        Jwts.builder()
            .setClaims(claims)
            .signWith(SignatureAlgorithm.HS512, secret)
            .setExpiration(exp)
            .compact();
    return new AuthToken(dateExp, token);
  }

  @LogInterceptor(type = LogTimeTracker.ActionType.APP_SERVICE)
  public User parseToken(AuthToken token) throws UtilsException {
    Claims body;
    try {
      body = Jwts.parser().setSigningKey(secret).parseClaimsJws(token.getAccessToken()).getBody();
    } catch (JwtException e) {
      LOG.error("Not Authorized, parseToken - Exception -> {}", e.getMessage());
      throw new UtilsException(AuthException.ERR_AUTH_MSS_002, e.getMessage());
    }

    return new User(
        (@NotNull String) body.get(FIRSTNAME),
        (@NotNull String) body.get(LASTNAME),
        (@NotNull String) body.get(EMAIL),
        body.getSubject(),
        UserRole.valueOf((@NotNull String) body.get(ROLE)),
        (@NotNull String) body.get(PROFILE_PHOTO),
        (@NotNull String) body.get(CURRENCY));
  }
}
