package com.giova.service.moneystats.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giova.service.moneystats.authentication.dto.User;
import com.giova.service.moneystats.authentication.entity.UserEntity;
import com.giova.service.moneystats.generic.Response;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AuthServiceTest {

  @Autowired private AuthService authService;

  private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

  @Test
  public void registerTest_successfully() throws IOException, UtilsException {
    Response expected =
        objectMapper.readValue(
            new ClassPathResource("mock/response/user.json").getInputStream(), Response.class);
    User user =
        objectMapper.readValue(
            new ClassPathResource("mock/request/user.json").getInputStream(), User.class);

    User userEx = objectMapper.convertValue(expected.getData(), User.class);
    String token = "token";

    ResponseEntity<Response> actual = authService.register(user, token);

    User userAc = objectMapper.convertValue(actual.getBody().getData(), User.class);
    assertEquals(expected.getStatus(), actual.getBody().getStatus());
    assertEquals(expected.getMessage(), actual.getBody().getMessage());
    assertEquals(userEx.getName(), userAc.getName());
    assertEquals(userEx.getUsername(), userAc.getUsername());
  }

  @Test
  public void loginTest_successfully() throws IOException, UtilsException {
    User register =
        objectMapper.readValue(
            new ClassPathResource("mock/request/user.json").getInputStream(), User.class);
    String token = "token";

    ResponseEntity<Response> actualR = authService.register(register, token);

    User registered = objectMapper.convertValue(actualR.getBody().getData(), User.class);

    Response expected =
        objectMapper.readValue(
            new ClassPathResource("mock/response/login.json").getInputStream(), Response.class);

    User user = objectMapper.convertValue(expected.getData(), User.class);

    ResponseEntity<Response> actual = authService.login(registered.getUsername(), "string");

    User userAc = objectMapper.convertValue(actual.getBody().getData(), User.class);
    assertEquals(expected.getStatus(), actual.getBody().getStatus());
    assertEquals(expected.getMessage(), actual.getBody().getMessage());
    assertEquals(user.getName(), userAc.getName());
    assertEquals(user.getUsername(), userAc.getUsername());
  }

  @Test
  public void checkLoginTest_successfully() throws IOException, UtilsException {
    User register =
        objectMapper.readValue(
            new ClassPathResource("mock/request/user.json").getInputStream(), User.class);
    String token = "token";

    ResponseEntity<Response> actualR = authService.register(register, token);

    User registered = objectMapper.convertValue(actualR.getBody().getData(), User.class);

    ResponseEntity<Response> actual = authService.login(registered.getUsername(), "string");

    User userAc = objectMapper.convertValue(actual.getBody().getData(), User.class);

    UserEntity checkLogin = authService.checkLogin(userAc.getAuthToken().getAccessToken());
    assertEquals(userAc.getName(), checkLogin.getName());
    assertEquals(userAc.getUsername(), checkLogin.getUsername());
  }

  @Test
  public void checkLoginFETest_successfully() throws IOException, UtilsException {
    User register =
        objectMapper.readValue(
            new ClassPathResource("mock/request/user.json").getInputStream(), User.class);
    String token = "token";

    ResponseEntity<Response> actualR = authService.register(register, token);

    User registered = objectMapper.convertValue(actualR.getBody().getData(), User.class);

    ResponseEntity<Response> actual = authService.login(registered.getUsername(), "string");

    User userAc = objectMapper.convertValue(actual.getBody().getData(), User.class);

    ResponseEntity<Response> checkLogin =
        authService.checkLoginFE(userAc.getAuthToken().getAccessToken());
    assertEquals(HttpStatus.OK, checkLogin.getStatusCode());
  }
}
