package com.ratelimiter.gateway_service.ratelimit;

import com.ratelimiter.gateway_service.model.ApiKeyConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory to create and cache RateLimiter instances based on API key configuration.
 */
@Component
public class RateLimiterFactory {

    private final RedisTemplate<String, String> stringRedisTemplate;
    private final RedisTemplate<String, Long> longRedisTemplate;
    private final Map<String, RateLimiter> rateLimiterCache = new ConcurrentHashMap<>();

    public RateLimiterFactory(
            RedisTemplate<String, String> stringRedisTemplate,
            @Qualifier("redisScriptTemplate") RedisTemplate<String, Long> longRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.longRedisTemplate = longRedisTemplate;
    }

    public RateLimiter getRateLimiter(ApiKeyConfig config) {
        return rateLimiterCache.computeIfAbsent(config.getApiKey(), key -> createRateLimiter(config));
    }

    private RateLimiter createRateLimiter(ApiKeyConfig config) {
        switch (config.getAlgorithm().toLowerCase()) {
            case "token-bucket":
                // Allow configurable refill rate from YAML, fallback to limit/10 if not set
                int refillRate = (config.getRefillRate() != null && config.getRefillRate() > 0)
                        ? config.getRefillRate()
                        : Math.max(1, config.getLimit() / 10);
                return new TokenBucketRateLimiter(longRedisTemplate, config.getLimit(), refillRate);

            case "sliding-window-log":
                // Default 60-second window
                return new SlidingWindowLogRateLimiter(stringRedisTemplate, config.getLimit(), 60);

            case "fixed-window":
            default:
                // Default 60-second window
                return new FixedWindowRateLimiter(stringRedisTemplate, config.getLimit(), 60);
        }
    }
}
