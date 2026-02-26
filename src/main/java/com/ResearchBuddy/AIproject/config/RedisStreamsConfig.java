package com.ResearchBuddy.AIproject.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RedisStreamsConfig {
  @Value("${app.research.redis.streams.job-stream-name}")

  private final String JOB_STREAM;
  @Value("${app.research.redis.streams.job-consumer-group}")

  private final String JOB_GROUP;
  @Value("${app.research.redis.streams.dead-letter-stream-name}")

  private final String JOB_DLQ_STREAM;

  @Bean
  ApplicationRunner initStreamsConfig(StringRedisTemplate redis) {
    return args -> {
      redis.opsForStream().add(MapRecord.create(JOB_STREAM, Map.of("start", "1")));

      try {
        redis.opsForStream().createGroup(JOB_STREAM, ReadOffset.latest(), JOB_GROUP);

      } catch (Exception e) {
        if (e.getMessage() == null || !e.getMessage().contains("BUSYGROUP"))
          throw e;

      }
    };

  }

}
