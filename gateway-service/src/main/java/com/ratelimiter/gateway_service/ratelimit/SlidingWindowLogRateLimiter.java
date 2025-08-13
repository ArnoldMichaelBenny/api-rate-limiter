package com.ratelimiter.gateway_service.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;

@RequiredArgsConstructor
@Slf4j
public class SlidingWindowLogRateLimiter implements RateLimiter {
    private final RedisTemplate<String, String> redisTemplate;
    private final int limit;
    private final int windowInSeconds;

    @Override
    public boolean isAllowed(String apiKey) {
        String key = "rate_limit:sliding_window:" + apiKey;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - (windowInSeconds * 1000L);

        // Remove old timestamps
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        // Get current request count
        Long currentRequests = redisTemplate.opsForZSet().zCard(key);

        if (currentRequests != null && currentRequests < limit) {
            redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
            log.debug("Client: {} has made {} requests, allowed: true", apiKey, currentRequests + 1);
            return true;
        }

        log.debug("Client: {} has made {} requests, allowed: false", apiKey, currentRequests);
        return false;
    }
}
