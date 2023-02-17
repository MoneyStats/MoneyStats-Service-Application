package com.giova.service.moneystats.authentication;

import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import org.springframework.http.HttpStatus;

public enum AuthException implements ExceptionCode {
  ERR_AUTH_MSS_001("AUTHENTICATION_EXCEPTION", HttpStatus.BAD_REQUEST, "Missing Value for: "),
  ERR_AUTH_MSS_002("TOKEN_PARSE", HttpStatus.UNAUTHORIZED, "Error during parsing Access-Token"),
  ERR_AUTH_MSS_003(
      "WRONG_CREDENTIAL",
      HttpStatus.BAD_REQUEST,
      "Wrong Credential for username or password. Try again!"),
  ERR_AUTH_MSS_004(
      "CHECK_LOGIN_FAIL",
      HttpStatus.UNAUTHORIZED,
      "Error on checking the current user, Login again!"),
  ERR_AUTH_MSS_005(
      "INVALID_REGISTER_TOKEN",
      HttpStatus.UNAUTHORIZED,
      "Error on checking the current token provided, wrong token, try again!");

  private final HttpStatus status;
  private final String message;
  private final String exceptionName;

  AuthException(String exceptionName, HttpStatus status, String message) {
    this.exceptionName = exceptionName;
    this.status = status;
    this.message = message;
  }

  @Override
  public String exceptionName() {
    return this.exceptionName;
  }

  @Override
  public String getMessage() {
    return this.message;
  }

  @Override
  public HttpStatus getStatus() {
    return this.status;
  }
}
