package com.giova.service.moneystats.generic;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@MappedSuperclass
public abstract class GenericEntity {

  @Column(name = "CREATION_DATE", updatable = false)
  private LocalDateTime creationDate;

  @Column(name = "UPDATE_DATE")
  private LocalDateTime updateDate;

  @Column(name = "DELETED_DATE")
  private LocalDateTime deletedDate;

  @PrePersist
  public void creationDates() {
    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    setCreationDate(now);
    setUpdateDate(now);
  }

  @PreUpdate
  public void updateDates() {
    setUpdateDate(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
  }
}
