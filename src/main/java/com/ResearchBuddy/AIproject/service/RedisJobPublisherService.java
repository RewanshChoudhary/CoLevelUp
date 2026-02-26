package com.ResearchBuddy.AIproject.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class RedisJobPublisherService {
  @Value("${app.research.redis.streams.job-stream-name}")
  private final String JOB_STREAM;

  private final StringRedisTemplate redis;

  public void publish(UUID jobId, UUID userID, String depth) {
    redis.opsForStream().add(MapRecord.create(JOB_STREAM,
        Map.of("jobId", jobId.toString(),
            "userID", userID.toString(),
            "depth", depth,
            "attempts", "0")));

  }

}
