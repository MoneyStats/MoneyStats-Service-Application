package com.giova.service.moneystats.api.emailSender.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmailResponse {

    private LocalDateTime timestamp;
    private String message;

    private String token;

    public EmailResponse() {
    this.timestamp = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}
