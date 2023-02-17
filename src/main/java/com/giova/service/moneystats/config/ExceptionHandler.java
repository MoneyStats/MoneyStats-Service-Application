package com.giova.service.moneystats.config;

import com.giova.service.moneystats.authentication.AuthException;
import io.github.giovannilamarmora.utils.exception.UtilsException;
import io.github.giovannilamarmora.utils.exception.dto.ExceptionResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;

@ControllerAdvice
public class ExceptionHandler extends UtilsException {

  @org.springframework.web.bind.annotation.ExceptionHandler(
      value = DataIntegrityViolationException.class)
  public ResponseEntity<ExceptionResponse> handleException(
      DataIntegrityViolationException e, HttpServletRequest request) {
    LOG.error(
        "An error happened while calling {} Downstream API: {}",
        request.getRequestURI(),
        e.getMessage());
    HttpStatus status = HttpStatus.BAD_REQUEST;
    ExceptionResponse response =
        getExceptionResponse(e, request, AuthException.ERR_AUTH_MSS_001, status);
    String value = "";
    if (response.getError().getExceptionMessage().contains("constraint")) {
      value = Objects.requireNonNull(e.getMessage()).split(";")[2];
    } else {
      value =
          AuthException.ERR_AUTH_MSS_001.getMessage()
              + e.getCause()
                  .getMessage()
                  .split("\\.")[e.getCause().getMessage().split("\\.").length - 1];
    }
    response.getError().setMessage(value);
    if (e.getStackTrace().length != 0) {
      response.getError().setStackTrace(Arrays.toString(e.getStackTrace()));
    }
    return new ResponseEntity<>(response, status);
  }

  /**
   * @org.springframework.web.bind.annotation.ExceptionHandler( value = RuntimeException.class)
   *     public ResponseEntity<ExceptionResponse> handleRuntimeException( RuntimeException e,
   *     HttpServletRequest request) { LOG.error( "An error happened while calling {} Downstream
   *     APIIII: {}", request.getRequestURI(), e.getMessage()); HttpStatus status =
   *     HttpStatus.BAD_REQUEST; ExceptionResponse response = getExceptionResponse(e, request,
   *     AuthException.ERR_AUTH_MSS_004, status);
   *     <p>response.getError().setMessage(e.getMessage()); return new ResponseEntity<>(response,
   *     status); }
   */
}
