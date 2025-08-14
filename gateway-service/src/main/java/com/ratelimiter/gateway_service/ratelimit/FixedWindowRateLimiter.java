package com.ratelimiter.gateway_service.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.Instant;

@RequiredArgsConstructor
@Slf4j
public class FixedWindowRateLimiter implements RateLimiter {
    private final RedisTemplate<String, String> redisTemplate;
    private final int limit;
    private final int windowInSeconds;

    @Override
    public boolean isAllowed(String apiKey) {
        long currentWindow = Instant.now().getEpochSecond() / windowInSeconds;
        String key = "rate_limit:" + apiKey + ":" + currentWindow;
        log.debug("Checking rate limit for client: {} with key: {}", apiKey, key);

        Long currentRequests = redisTemplate.opsForValue().increment(key);

        if (currentRequests != null && currentRequests == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowInSeconds));
        }

        boolean allowed = currentRequests != null && currentRequests <= limit;
        log.debug("Client: {} has made {} requests, allowed: {}", apiKey, currentRequests, allowed);

        return allowed;
    }
}
