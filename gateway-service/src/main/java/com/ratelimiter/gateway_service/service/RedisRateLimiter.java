package com.ratelimiter.gateway_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${rate-limiter.requests-per-minute:10}")
    private int maxRequestsPerMinute;

    public boolean isAllowed(String clientId) {
        long currentMinute = Instant.now().getEpochSecond() / 60;
        String key = "rate_limit:" + clientId + ":" + currentMinute;
        log.debug("Checking rate limit for client: {} with key: {}", clientId, key);

        Long currentRequests = redisTemplate.opsForValue().increment(key);

        if (currentRequests != null && currentRequests == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(60));
        }

        boolean allowed = currentRequests != null && currentRequests <= maxRequestsPerMinute;
        log.debug("Client: {} has made {} requests, allowed: {}", clientId, currentRequests, allowed);

        return allowed;
    }
}
