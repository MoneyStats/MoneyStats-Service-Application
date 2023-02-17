package com.giova.service.moneystats.app.stats.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.giova.service.moneystats.generic.GenericDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Stats extends GenericDTO {

  private LocalDate date;
  private Double balance;
  private Double percentage;
  private Double trend;
}
