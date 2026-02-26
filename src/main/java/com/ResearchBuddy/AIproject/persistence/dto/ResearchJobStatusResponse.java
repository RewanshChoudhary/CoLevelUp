package com.ResearchBuddy.AIproject.persistence.dto;

import com.ResearchBuddy.AIproject.persistence.dto.enums.JobStatusType;
import com.ResearchBuddy.AIproject.persistence.dto.enums.ResearchDepthType;
import com.ResearchBuddy.AIproject.persistence.dto.enums.ResearchDomainType;
import com.ResearchBuddy.AIproject.persistence.dto.enums.ResearchStageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class ResearchJobStatusResponse {

  @NotNull
  private UUID jobId;

  @NotNull
  private JobStatusType status;

  private ResearchStageType currentStage;

  @Min(0)
  @Max(100)
  private Integer progressPercent;

  private String query;

  private ResearchDomainType domain;

  private ResearchDepthType depth;

  private Instant createdAt;

  private Instant startedAt;

  private Instant completedAt;

  @PositiveOrZero
  private Long elapsedTimeMs;

  private ResearchReportResponse report;

  private JobErrorDetail error;
}
