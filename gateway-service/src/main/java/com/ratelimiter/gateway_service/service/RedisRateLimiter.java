package com.ratelimiter.gateway_service.service;

import com.ratelimiter.gateway_service.dto.RequestLog;
import jakarta.servlet.http.HttpServletRequest;
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
    private final MonitoringClient monitoringClient;

    @Value("${rate-limiter.requests-per-minute}")
    private int maxRequestsPerMinute;

    public boolean isAllowed(String clientId, HttpServletRequest request) {
        long currentMinute = Instant.now().getEpochSecond() / 60;
        String key = "rate_limit:" + clientId + ":" + currentMinute;

        Long currentRequests = redisTemplate.opsForValue().increment(key);

        // Set expiry only for the first request in the window
        if (currentRequests != null && currentRequests == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }

        boolean allowed = currentRequests != null && currentRequests <= maxRequestsPerMinute;

        // Log the request asynchronously to the monitoring service
        RequestLog requestLog = new RequestLog(
                clientId,
                request.getRequestURI(),
                Instant.now(),
                allowed,
                request.getMethod()
        );
        monitoringClient.logRequest(requestLog);

        return allowed;
    }
}
