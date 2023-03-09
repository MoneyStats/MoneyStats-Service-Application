package com.giova.service.moneystats.authentication.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.authentication.dto.UserRole;
import com.giova.service.moneystats.generic.GenericEntity;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "USERS")
public class UserEntity extends GenericEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID", nullable = false)
  private Long id;

  @Column(name = "NAME", nullable = false)
  private String name;

  @Column(name = "SURNAME", nullable = false)
  private String surname;

  @Column(name = "EMAIL", nullable = false, unique = true)
  private String email;

  @Column(name = "USERNAME", nullable = false, unique = true)
  private String username;

  @Column(name = "PASSWORD")
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "ROLE", nullable = false)
  private UserRole role;

  @Column(name = "PROFILE_PHOTO", nullable = false)
  private String profilePhoto;

  // @Enumerated(EnumType.STRING)
  @Column(name = "CURRENCY", nullable = false)
  private String currency;

  @Lob
  @Column(name = "GITHUB_USER")
  private String githubUser;

  @Column(name = "TOKEN_RESET")
  private String tokenReset;
}
