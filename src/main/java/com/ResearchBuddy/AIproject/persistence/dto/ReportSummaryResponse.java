package com.ResearchBuddy.AIproject.persistence.dto;

import com.ResearchBuddy.AIproject.persistence.dto.enums.ResearchDepthType;
import com.ResearchBuddy.AIproject.persistence.dto.enums.ResearchDomainType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ReportSummaryResponse {

  private UUID reportId;

  private String query;

  private ResearchDomainType domain;

  private ResearchDepthType depth;

  @DecimalMin(value = "0.000", inclusive = true)
  @DecimalMax(value = "1.000", inclusive = true)
  @Digits(integer = 1, fraction = 3)
  private BigDecimal confidenceScore;

  @PositiveOrZero
  private Integer totalSourcesProcessed;

  @PositiveOrZero
  private Long totalTimeMs;

  private Instant createdAt;
}
