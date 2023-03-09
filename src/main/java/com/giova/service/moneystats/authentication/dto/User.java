package com.giova.service.moneystats.authentication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.authentication.token.dto.AuthToken;
import com.giova.service.moneystats.generic.GenericDTO;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User extends GenericDTO {
  @NotNull private String name;
  @NotNull private String surname;
  @NotNull private String email;
  @NotNull private String username;
  @NotNull private String password;
  private UserRole role;
  @NotNull private String profilePhoto;
  @NotNull private String currency;
  private String githubUser;

  private AuthToken authToken;
  private String tokenReset;

  public User(
      String name,
      String surname,
      String email,
      String username,
      UserRole role,
      String profilePhoto,
      String currency) {
    this.name = name;
    this.surname = surname;
    this.email = email;
    this.username = username;
    this.role = role;
    this.profilePhoto = profilePhoto;
    this.currency = currency;
  }
}
