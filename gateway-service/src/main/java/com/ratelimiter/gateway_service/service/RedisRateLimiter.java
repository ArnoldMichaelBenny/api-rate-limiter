// The file path is now: ...\gateway_service\service\RedisRateLimiter.java

package com.ratelimiter.gateway_service.service; // <-- THIS LINE IS UPDATED

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${rate-limiter.requests-per-minute}")
    private int maxRequestsPerMinute;

    public boolean isAllowed(String clientId) {
        long currentMinute = Instant.now().getEpochSecond() / 60;
        String key = "rate_limit:" + clientId + ":" + currentMinute;

        Long currentRequests = redisTemplate.opsForValue().increment(key);

        if (currentRequests != null && currentRequests == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(60));
        }

        return currentRequests != null && currentRequests <= maxRequestsPerMinute;
    }
}