package com.ratelimiter.gateway_service.filter;

import com.ratelimiter.gateway_service.client.MonitoringClient;
import com.ratelimiter.gateway_service.service.RateLimiterService;
import com.ratelimiter.shared.dto.RequestLogDto;
import com.ratelimiter.shared.enums.RequestStatus;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
// ✅ Only one import is needed
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final MonitoringClient monitoringClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = getApiKey(request);
        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String requestURI = request.getRequestURI();
        log.debug("Processing request from api key: {} for URI: {}", apiKey, requestURI);

        if (rateLimiterService.isAllowed(apiKey)) {
            log.debug("Request allowed for api key: {}", apiKey);
            logRequest(request, RequestStatus.ALLOWED);
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for api key: {} on URI: {}", apiKey, requestURI);
            logRequest(request, RequestStatus.BLOCKED);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again later.\"}");
        }
    }

    private void logRequest(HttpServletRequest request, RequestStatus status) {
        RequestLogDto logDto = new RequestLogDto(
                request.getRemoteAddr(),
                Instant.now(),
                status,
                request.getRequestURI()
        );
        monitoringClient.logRequest(logDto);
    }

    private String getApiKey(HttpServletRequest request) {
        return request.getHeader("X-API-KEY");
    }
}