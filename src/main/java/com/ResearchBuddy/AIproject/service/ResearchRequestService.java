package com.ResearchBuddy.AIproject.service;

import java.time.ZoneOffset;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.ResearchBuddy.AIproject.persistence.dto.ResearchJobAcceptedResponse;
import com.ResearchBuddy.AIproject.persistence.dto.ResearchRequest;
import com.ResearchBuddy.AIproject.persistence.entity.ResearchJobEntity;
import com.ResearchBuddy.AIproject.persistence.entity.UserEntity;
import com.ResearchBuddy.AIproject.persistence.entity.enums.JobStatus;
import com.ResearchBuddy.AIproject.persistence.repository.ResearchJobRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResearchRequestService {
  private final RedisJobPublisherService publisher;
  private final ResearchJobRepository researchJobRepository;

  @Transactional
  public ResearchJobAcceptedResponse createRequest(UserEntity user, ResearchRequest request) {
    ResearchJobEntity job = ResearchJobEntity.builder().user(user)
        .status(JobStatus.PENDING)
        .depth(request.getDepth())
        .maxSources(request.getMaxSources())
        .domain(request.getDomain())
        .factCheckEnabled(request.getFactCheck())
        .query(request.getQuery())
        .build();
    researchJobRepository.save(job);

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        publisher.publish(job.getId(), user.getId(), request.getDepth().name());
      }
    });
    return ResearchJobAcceptedResponse.builder()
        .jobId(job.getId())
        .status(JobStatus.PENDING)
        .estimatedTimeSeconds(30)
        .createdAt(job.getCreatedAt().atZone(ZoneOffset.UTC).toInstant())
        .pollUrl("/api/research/jobs/" + job.getId())
        .build();

  }

  public ResearchJobAcceptedResponse getStatus(String jobId, UUID id) {


  }

}
