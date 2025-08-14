package com.ratelimiter.gateway_service.ratelimit;

public interface RateLimiter {
    boolean isAllowed(String apiKey);
}
