package com.giova.service.moneystats.app.attachments;

import com.giova.service.moneystats.exception.ExceptionMap;
import io.github.giovannilamarmora.utils.exception.ExceptionCode;
import io.github.giovannilamarmora.utils.exception.UtilsException;

public class ImageException extends UtilsException {

  private static final ExceptionCode DEFAULT_CODE = ExceptionMap.ERR_IMG_MSS_001;

  public ImageException(String message) {
    super(DEFAULT_CODE, message);
  }

  public ImageException(String message, String exceptionMessage) {
    super(DEFAULT_CODE, message, exceptionMessage);
  }
}
