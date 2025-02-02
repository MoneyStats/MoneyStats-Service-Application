package com.giova.service.moneystats.utilities;

import com.giova.service.moneystats.app.settings.dto.Status;
import com.giova.service.moneystats.authentication.dto.UserData;
import io.github.giovannilamarmora.utils.utilities.Utilities;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.ObjectUtils;

public class Utils {

  public static Boolean isLiveWallet(Boolean live, UserData user) {
    /**
     * We give the priority to the param "Boolean live", if the param is null we check the User
     * Setting if it has the live wallet status ACTIVE. For the FrontEnd is Recommended to use this
     * value as Null
     */
    return !ObjectUtils.isEmpty(live)
        ? live
        : !ObjectUtils.isEmpty(user.getSettings())
            && !ObjectUtils.isEmpty(user.getSettings().getLiveWallets())
            && user.getSettings().getLiveWallets().equalsIgnoreCase(Status.ACTIVE.toString());
  }

  public static boolean checkCharacterAndRegexValid(String field, String regex) {
    if (ObjectUtils.isEmpty(field) || ObjectUtils.isEmpty(regex)) return false;
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(field);
    return m.find();
  }

  public static Long convertToSize(String size) {
    if (Utilities.isNullOrEmpty(size)) {
      throw new IllegalArgumentException("Size must not be null or empty");
    }

    String unit = size.replaceAll("[0-9]", "").trim().toUpperCase();
    long value = Long.parseLong(size.replaceAll("[^0-9]", "").trim());

    return switch (unit) {
      case "KB" -> value * 1024; // 1 KB = 1024 bytes
      case "MB" -> value * 1024 * 1024; // 1 MB = 1024 * 1024 bytes
      case "GB" -> value * 1024 * 1024 * 1024; // 1 GB = 1024 * 1024 * 1024 bytes
      case "B" -> value; // Already in bytes
      default -> throw new IllegalArgumentException("Unsupported size unit: " + unit);
    };
  }

  public static <E extends Enum<E>> boolean isEnumValue(String value, Class<E> enumClass) {
    try {
      Enum.valueOf(enumClass, value);
      return true;
    } catch (IllegalArgumentException | NullPointerException e) {
      return false;
    }
  }
}
