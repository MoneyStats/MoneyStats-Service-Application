package com.giova.service.moneystats.app;

import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import org.springframework.http.HttpStatus;

public enum AppException implements ExceptionCode {
  ERR_APP_MSS_001("MATH_EXCEPTION", HttpStatus.BAD_REQUEST, "Error on rounding value, try again!");

  private final HttpStatus status;
  private final String message;
  private final String exceptionName;

  AppException(String exceptionName, HttpStatus status, String message) {
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
