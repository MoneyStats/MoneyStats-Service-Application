package com.giova.service.moneystats.generic;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericDTO {

  private Long id;

  private LocalDateTime creationDate;

  private LocalDateTime updateDate;

  private LocalDateTime deletedDate;
}
