package com.ResearchBuddy.AIproject.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisConsumerService {
  @Value("${app.research.redis.streams.job-stream-name}")
  private String JOB_STREAM;
  @Value("${app.research.redis.streams.job-consumer-group}")
  private String JOB_GROUP;
  @Value("${app.research.redis.streams.dead-letter-stream-name}")
  private String DLQ_STREAM;
  private String DELAYED_STREAM = "research:stream:delayed";

  private static final int MAX_RETRIES = 3;
  private static final long RETRY_DELAY_MS = 5000; // 5 seconds
  private static final long STALE_THRESHOLD_MS = 60000; // 60 seconds

  private final String consumerName = "worker-" + UUID.randomUUID();

  private final StringRedisTemplate redis;
  // private final ResearchPipelineOrchestrator orchestrator;

  // --------------------------
  // 1️⃣ Main Consumption Loop
  // --------------------------
  @Scheduled(fixedDelay = 1000)
  public void consume() {

    List<MapRecord<String, Object, Object>> batch = redis.opsForStream().read(
        Consumer.from(JOB_GROUP, consumerName),
        StreamReadOptions.empty().count(10).block(Duration.ofSeconds(2)),
        StreamOffset.create(JOB_STREAM, ReadOffset.lastConsumed()));

    if (batch == null || batch.isEmpty())
      return;

    for (MapRecord<String, Object, Object> record : batch) {
      handleRecord(record);
    }
  }

  private void handleRecord(MapRecord<String, Object, Object> record) {
    try {
      UUID jobId = UUID.fromString(String.valueOf(record.getValue().get("jobId")));
      // orchestrator.process(jobId);

      redis.opsForStream().acknowledge(
          JOB_STREAM,
          JOB_GROUP,
          record.getId());

    } catch (Exception ex) {
      retryOrDlq(record, ex);
    }
  }

  // --------------------------
  // 2️⃣ Retry with Backoff
  // --------------------------
  private void retryOrDlq(MapRecord<String, Object, Object> record, Exception ex) {

    int attempt = Integer.parseInt(
        String.valueOf(record.getValue().getOrDefault("attempt", "0")));

    String jobId = String.valueOf(record.getValue().get("jobId"));

    if (attempt + 1 >= MAX_RETRIES) {

      redis.opsForStream().add(MapRecord.create(
          DLQ_STREAM,
          Map.of(
              "jobId", jobId,
              "reason", ex.getMessage(),
              "attempt", String.valueOf(attempt + 1),
              "failedAt", String.valueOf(System.currentTimeMillis()))));

    } else {

      long retryAt = System.currentTimeMillis() + RETRY_DELAY_MS;

      redis.opsForStream().add(MapRecord.create(
          DELAYED_STREAM,
          Map.of(
              "jobId", jobId,
              "attempt", String.valueOf(attempt + 1),
              "retryAt", String.valueOf(retryAt))));
    }

    redis.opsForStream().acknowledge(
        JOB_STREAM,
        JOB_GROUP,
        record.getId());
  }

  // --------------------------
  // 3️⃣ Process Delayed Jobs
  // --------------------------
  @Scheduled(fixedDelay = 2000)
  public void processDelayed() {

    List<MapRecord<String, Object, Object>> batch = redis.opsForStream().read(
        StreamReadOptions.empty().count(10),
        StreamOffset.fromStart(DELAYED_STREAM));

    if (batch == null || batch.isEmpty())
      return;

    long now = System.currentTimeMillis();

    for (MapRecord<String, Object, Object> record : batch) {

      long retryAt = Long.parseLong(
          String.valueOf(record.getValue().get("retryAt")));

      if (retryAt <= now) {

        redis.opsForStream().add(MapRecord.create(
            JOB_STREAM,
            Map.of(
                "jobId", record.getValue().get("jobId"),
                "attempt", record.getValue().get("attempt"))));

        redis.opsForStream().delete(
            DELAYED_STREAM,
            record.getId());
      }
    }
  }

  // --------------------------
  // 4️⃣ Dead Worker Recovery
  // --------------------------
  @Scheduled(fixedDelay = 10000)
  public void recoverStale() {

    redis.opsForStream().autoClaim(
        JOB_STREAM,
        Consumer.from(JOB_GROUP, consumerName),
        Duration.ofMillis(STALE_THRESHOLD_MS),
        ReadOffset.from("0-0"));
  }
}
