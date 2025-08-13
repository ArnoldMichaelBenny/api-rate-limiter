package com.ratelimiter.gateway_service.interceptor;

import com.ratelimiter.gateway_service.client.MonitoringClient;
import com.ratelimiter.gateway_service.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// This interceptor is part of the old, disabled implementation.
// It is being repaired to allow the project to compile.
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimiterInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;
    // private final MonitoringClient monitoringClient; // Temporarily disabled

    @Value("${rate-limiter.requests-per-minute:10}")
    private int requestsPerMinute;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String apiKey = request.getHeader("X-API-KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "API Key is missing");
            return false;
        }

        if (!rateLimiterService.isAllowed(apiKey)) {
            log.warn("Rate limit exceeded for API key: {}", apiKey);
            // monitoringClient.log("Rate limit exceeded for API key: " + apiKey);
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), "Rate limit exceeded");
            return false;
        }

        return true;
    }
}
