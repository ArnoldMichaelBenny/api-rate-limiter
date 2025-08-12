package com.ratelimiter.gateway_service.security;

import com.ratelimiter.gateway_service.config.ApiKeyProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String API_SECRET_HEADER = "X-API-SECRET";

    private final ApiKeyProperties apiKeyProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestApiKey = request.getHeader(API_KEY_HEADER);
        String requestApiSecret = request.getHeader(API_SECRET_HEADER);

        if (requestApiKey == null || requestApiSecret == null) {
            log.warn("Missing API Key/Secret headers");
            filterChain.doFilter(request, response);
            return;
        }

        if (isValidApiKey(requestApiKey, requestApiSecret)) {
            log.info("Successfully authenticated API key: {}", requestApiKey);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    requestApiKey,
                    null,
                    AuthorityUtils.createAuthorityList("ROLE_API_USER")
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("Invalid API Key provided: {}", requestApiKey);
            // Do not set authentication, subsequent filters will deny access
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidApiKey(String key, String secret) {
        return apiKeyProperties.getKeys().stream()
                .anyMatch(apiKey -> apiKey.getKey().equals(key) && apiKey.getSecret().equals(secret));
    }
}