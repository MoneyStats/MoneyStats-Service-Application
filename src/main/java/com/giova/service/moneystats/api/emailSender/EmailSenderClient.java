package com.giova.service.moneystats.api.emailSender;

import com.giova.service.moneystats.api.emailSender.dto.EmailContent;
import com.giova.service.moneystats.api.emailSender.dto.EmailResponse;
import io.github.giovannilamarmora.utils.interceptors.Logged;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Logged
public class EmailSenderClient {

  @Value(value = "${rest.client.emailSender.url}")
  private String emailSenderUrl;

  @Value(value = "${rest.client.emailSender.sendEmailUrl}")
  private String sendEmailUrl;

  private final RestTemplate restTemplate = new RestTemplate();

  public ResponseEntity<EmailResponse> sendEmail(EmailContent emailContent) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Accept", "application/json");
    HttpEntity<EmailContent> request = new HttpEntity<>(emailContent, headers);
    String urlTemplate =
        UriComponentsBuilder.fromHttpUrl(emailSenderUrl + sendEmailUrl)
            .queryParam("htmlText", true)
            .encode()
            .toUriString();
    ResponseEntity<EmailResponse> response =
        restTemplate.exchange(urlTemplate, HttpMethod.POST, request, EmailResponse.class);

    return response;
  }
}
