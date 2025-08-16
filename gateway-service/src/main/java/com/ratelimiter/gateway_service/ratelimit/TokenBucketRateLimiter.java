package com.ratelimiter.gateway_service.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
public class TokenBucketRateLimiter implements RateLimiter {
    private final RedisTemplate<String, Long> redisTemplate;
    private final int capacity;
    private final int refillRatePerMinute;

    public TokenBucketRateLimiter(@Qualifier("redisScriptTemplate") RedisTemplate<String, Long> redisTemplate, int capacity, int refillRatePerMinute) {
        this.redisTemplate = redisTemplate;
        this.capacity = capacity;
        this.refillRatePerMinute = refillRatePerMinute;
    }

    // ✅ A simpler, more robust, and battle-tested Lua script.
    private static final RedisScript<Long> SCRIPT = new DefaultRedisScript<>(
            "local tokens_key = KEYS[1] " +
                    "local last_refill_key = KEYS[2] " +
                    "local capacity = tonumber(ARGV[1]) " +
                    "local refill_rate_per_second = tonumber(ARGV[2]) " +
                    "local now = tonumber(ARGV[3]) " +
                    "local requested_tokens = 1 " +

                    "local current_tokens = tonumber(redis.call('get', tokens_key)) " +
                    "if current_tokens == nil then " +
                    "  current_tokens = capacity " +
                    "end " +

                    "local last_refill = tonumber(redis.call('get', last_refill_key)) " +
                    "if last_refill == nil then " +
                    "  last_refill = now " +
                    "end " +

                    "local elapsed = now - last_refill " +
                    "if elapsed > 0 then " +
                    "  local tokens_to_add = math.floor(elapsed * refill_rate_per_second) " +
                    "  current_tokens = math.min(current_tokens + tokens_to_add, capacity) " +
                    "  redis.call('set', last_refill_key, now) " +
                    "end " +

                    "if current_tokens >= requested_tokens then " +
                    "  current_tokens = current_tokens - requested_tokens " +
                    "  redis.call('set', tokens_key, current_tokens) " +
                    "  return 1 " +
                    "else " +
                    "  return 0 " +
                    "end", Long.class);

    @Override
    public boolean isAllowed(String apiKey) {
        double refillRatePerSecond = (double) refillRatePerMinute / 60.0;
        List<String> keys = List.of("rate_limit:bucket:" + apiKey + ":tokens", "rate_limit:bucket:" + apiKey + ":last_refill");
        long now = System.currentTimeMillis() / 1000;

        try {
            Long result = redisTemplate.execute(SCRIPT, keys,
                    String.valueOf(capacity),
                    String.valueOf(refillRatePerSecond),
                    String.valueOf(now)
            );
            boolean allowed = result != null && result == 1;
            log.debug("Client: {} token bucket check, allowed: {}", apiKey, allowed);
            return allowed;
        } catch (Exception e) {
            log.error("Error executing token bucket script for key {}: {}", apiKey, e.getMessage());
            // Fail open: If the script fails, allow the request to prevent false negatives.
            return true;
        }
    }
}
