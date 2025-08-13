package com.ratelimiter.gateway_service.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class TokenBucketRateLimiter implements RateLimiter {
    private final RedisTemplate<String, String> redisTemplate;
    private final int capacity;
    private final int refillRatePerMinute;

    private static final RedisScript<Long> SCRIPT = new DefaultRedisScript<>(
            "local tokens_key = KEYS[1] " +
            "local last_refill_key = KEYS[2] " +
            "local capacity = tonumber(ARGV[1]) " +
            "local refill_rate = tonumber(ARGV[2]) " +
            "local now = tonumber(ARGV[3]) " +
            "local current_tokens = tonumber(redis.call('get', tokens_key) or capacity) " +
            "local last_refill = tonumber(redis.call('get', last_refill_key) or now) " +
            "local elapsed_time = now - last_refill " +
            "if elapsed_time > 0 then " +
            "  local new_tokens = math.floor(elapsed_time / 60) * refill_rate " +
            "  if new_tokens > 0 then " +
            "    current_tokens = math.min(current_tokens + new_tokens, capacity) " +
            "    redis.call('set', last_refill_key, now) " +
            "  end " +
            "end " +
            "if current_tokens > 0 then " +
            "  redis.call('set', tokens_key, current_tokens - 1) " +
            "  return 1 " +
            "else " +
            "  redis.call('set', tokens_key, 0) " +
            "  return 0 " +
            "end", Long.class);

    @Override
    public boolean isAllowed(String apiKey) {
        List<String> keys = Arrays.asList("rate_limit:bucket:" + apiKey, "rate_limit:last_refill:" + apiKey);
        long now = System.currentTimeMillis() / 1000;
        Long result = redisTemplate.execute(SCRIPT, keys, String.valueOf(capacity), String.valueOf(refillRatePerMinute), String.valueOf(now));
        boolean allowed = result != null && result == 1;
        log.debug("Client: {} token bucket check, allowed: {}", apiKey, allowed);
        return allowed;
    }
}
