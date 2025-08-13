package com.ratelimiter.gateway_service.service;

import com.ratelimiter.gateway_service.config.RateLimiterProperties;
import com.ratelimiter.gateway_service.model.ApiKeyConfig;
import com.ratelimiter.gateway_service.ratelimit.RateLimiter;
import com.ratelimiter.gateway_service.ratelimit.RateLimiterFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {
    private final RateLimiterFactory rateLimiterFactory;
    private final RateLimiterProperties rateLimiterProperties;
    private Map<String, ApiKeyConfig> apiKeyConfigs;

    @PostConstruct
    public void init() {
        log.info("Initializing RateLimiterService...");
        List<ApiKeyConfig> configs = rateLimiterProperties.getConfigs();
        if (configs == null || configs.isEmpty()) {
            log.warn("No specific API key configurations found in rate-limiter.configs. Using defaults.");
            apiKeyConfigs = new ConcurrentHashMap<>();
        } else {
            log.info("Found {} API key configurations to load.", configs.size());
            configs.forEach(config -> log.info("Loaded config: key='{}', algorithm='{}', limit={}", config.getApiKey(), config.getAlgorithm(), config.getLimit()));
            apiKeyConfigs = configs.stream()
                    .collect(Collectors.toMap(
                            ApiKeyConfig::getApiKey,
                            Function.identity(),
                            (existingConfig, newConfig) -> {
                                log.warn("Duplicate API key configuration found for key '{}'. The existing configuration will be used.", newConfig.getApiKey());
                                return existingConfig;
                            },
                            ConcurrentHashMap::new));
        }
        log.info("RateLimiterService initialized with {} specific configurations.", apiKeyConfigs.size());
    }

    /**
     * ✅ Simplified and corrected logic for checking if a request is allowed.
     */
    public boolean isAllowed(String apiKey) {
        // 1. Get the specific config, or create a default one if not found.
        ApiKeyConfig config = apiKeyConfigs.get(apiKey);
        if (config == null) {
            config = createDefaultConfig(apiKey);
        }

        // 2. Let the factory handle getting the correct cached rate limiter.
        RateLimiter rateLimiter = rateLimiterFactory.getRateLimiter(config);

        // 3. Check if the request is allowed.
        return rateLimiter.isAllowed(apiKey);
    }

    /**
     * ✅ Updated to use the new configurable default algorithm property.
     */
    private ApiKeyConfig createDefaultConfig(String apiKey) {
        log.warn("API key '{}' not found in configuration. Applying default settings.", apiKey);
        return new ApiKeyConfig(
                apiKey,
                rateLimiterProperties.getDefaultAlgorithm(),
                rateLimiterProperties.getDefaultLimit()
        );
    }
}