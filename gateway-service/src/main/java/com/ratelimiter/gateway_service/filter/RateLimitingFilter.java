package com.ratelimiter.gateway_service.filter;

import com.ratelimiter.gateway_service.service.RedisRateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RedisRateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientId = getClientId(request);
        String requestURI = request.getRequestURI();
        log.debug("Processing request from client: {} for URI: {}", clientId, requestURI);

        if (rateLimiter.isAllowed(clientId)) {
            log.debug("Request allowed for client: {}", clientId);
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for client: {} on URI: {}", clientId, requestURI);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again later.\"}");
        }
    }

    private String getClientId(HttpServletRequest request) {
        // Try to get client IP from various headers (for load balancers/proxies)
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
