package com.ratelimiter.gateway_service.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

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

    private static final RedisScript<Long> SCRIPT = new DefaultRedisScript<>(
            "local bucket_key = KEYS[1] " +
                    "local capacity = tonumber(ARGV[1]) " +
                    "local refill_rate_per_second = tonumber(ARGV[2]) " +
                    "local now = tonumber(ARGV[3]) " +

                    "local bucket_data = redis.call('hgetall', bucket_key) " +
                    "local tokens " +
                    "local last_refill " +

                    "if #bucket_data == 0 then " +
                    "  -- First request, initialize the bucket. " +
                    "  tokens = capacity " +
                    "  last_refill = now " +
                    "else " +
                    "  -- Bucket exists, calculate refill. " +
                    "  tokens = tonumber(bucket_data[2]) " +
                    "  last_refill = tonumber(bucket_data[4]) " +
                    "  local elapsed = now - last_refill " +
                    "  if elapsed > 0 then " +
                    "    local tokens_to_add = elapsed * refill_rate_per_second " +
                    "    tokens = math.min(tokens + tokens_to_add, capacity) " +
                    "    last_refill = now " + // ✅ FIX: Corrected typo from 'last_refiff' to 'last_refill'
                    "  end " +
                    "end " +

                    "if tokens >= 1 then " +
                    "  -- Consume a token and update the bucket. " +
                    "  tokens = tokens - 1 " +
                    "  redis.call('hmset', bucket_key, 'tokens', tokens, 'last_refill', last_refill) " +
                    "  redis.call('expire', bucket_key, 3600) -- Set an expiry to clean up old keys " +
                    "  return 1 " +
                    "else " +
                    "  -- Not enough tokens. " +
                    "  return 0 " +
                    "end", Long.class);

    @Override
    public boolean isAllowed(String apiKey) {
        double refillRatePerSecond = (double) refillRatePerMinute / 60.0;
        // The script now operates on a single key for the HASH
        List<String> keys = Collections.singletonList("rate_limit:bucket:" + apiKey);
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
