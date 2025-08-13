package com.ratelimiter.gateway_service.ratelimit;

import com.ratelimiter.gateway_service.model.ApiKeyConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimiterFactory {
    private final RedisTemplate<String, String> redisTemplate;

    // ✅ This cache now holds ALL rate limiters to ensure defaults are also cached.
    private final Map<String, RateLimiter> rateLimiterCache = new ConcurrentHashMap<>();

    /**
     * Gets a cached RateLimiter instance based on the given configuration.
     * If a limiter for the API key doesn't exist in the cache, it creates one
     * and caches it before returning. This is the single entry point for getting a limiter.
     */
    public RateLimiter getRateLimiter(ApiKeyConfig config) {
        // computeIfAbsent ensures thread-safe, atomic creation and caching.
        return rateLimiterCache.computeIfAbsent(config.getApiKey(), key -> createRateLimiter(config));
    }

    /**
     * ✅ Now a private helper method. The factory manages its own creation logic internally.
     * Creates a new RateLimiter instance based on the algorithm specified in the config.
     */
    private RateLimiter createRateLimiter(ApiKeyConfig config) {
        switch (config.getAlgorithm()) {
            case "token-bucket":
                return new TokenBucketRateLimiter(redisTemplate, config.getLimit(), config.getLimit() / 2);
            case "sliding-window-log":
                return new SlidingWindowLogRateLimiter(redisTemplate, config.getLimit(), 60);
            case "fixed-window":
            default:
                return new FixedWindowRateLimiter(redisTemplate, config.getLimit(), 60);
        }
    }
}