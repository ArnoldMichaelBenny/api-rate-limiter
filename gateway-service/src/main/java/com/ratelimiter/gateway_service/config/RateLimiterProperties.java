package com.ratelimiter.gateway_service.config;

import com.ratelimiter.gateway_service.model.ApiKeyConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "rate-limiter")
@Data
public class RateLimiterProperties {
    private int defaultLimit = 10;
    // ✅ Added a configurable default algorithm with a safe default value.
    private String defaultAlgorithm = "fixed-window";
    private List<ApiKeyConfig> configs = new ArrayList<>();
}