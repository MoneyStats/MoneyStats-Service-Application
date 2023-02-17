package com.giova.service.moneystats.authentication.token.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthToken {

  public static final String AUTH_TOKEN_HEADER_NAME = "authToken";

  private Long expirationTime;
  private String accessToken;
}
