package com.ratelimiter.gateway_service.filter;

import com.ratelimiter.gateway_service.service.RateLimiterService;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = getApiKey(request);
        String requestURI = request.getRequestURI();
        log.debug("Processing request from api key: {} for URI: {}", apiKey, requestURI);

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API key is missing");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"API key is missing.\"}");
            return;
        }

        if (rateLimiterService.isAllowed(apiKey)) {
            log.debug("Request allowed for api key: {}", apiKey);
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for api key: {} on URI: {}", apiKey, requestURI);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Try again later.\"}");
        }
    }

    private String getApiKey(HttpServletRequest request) {
        return request.getHeader("X-API-KEY");
    }
}
