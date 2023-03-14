package com.giova.service.moneystats.api.emailSender.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailContent {

  public static final String RESET_TEMPLATE = "EMAIL_RESET.html";
  public static final String CONTACT_TEMPLATE = "CONTACT_EMAIL.html";
  private String bbc;
  private String cc;
  private String from;
  private String replyTo;
  private Date sentDate;

  @NotNull(message = "Subject cannot be null")
  @NotBlank(message = "Subject cannot be blank")
  private String subject;

  @NotNull(message = "Text cannot be null")
  @NotBlank(message = "Text cannot be blank")
  private String text;

  @NotNull(message = "Email Destination cannot be null")
  @NotBlank(message = "Email Destination cannot be blank")
  private String to;
}
