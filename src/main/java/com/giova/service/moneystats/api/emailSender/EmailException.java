package com.giova.service.moneystats.api.emailSender;

import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import org.springframework.http.HttpStatus;

public enum EmailException implements ExceptionCode {
  ERR_EMAIL_SEND_001(
          "CLIENT_EXCEPTION",
          HttpStatus.BAD_REQUEST,
          "Error on client: "),
  ERR_EMAIL_SEND_002(
          "STRING_EXCEPTION",
          HttpStatus.BAD_REQUEST,
          "Error on converting string for templates");

  private final HttpStatus status;
  private final String message;
  private final String exceptionName;

  EmailException(String exceptionName, HttpStatus status, String message) {
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
