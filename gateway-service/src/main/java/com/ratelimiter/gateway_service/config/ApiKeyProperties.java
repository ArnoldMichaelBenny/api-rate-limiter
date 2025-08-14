package com.ratelimiter.gateway_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "api")
@Data
public class ApiKeyProperties {

    private List<Key> keys = new ArrayList<>();

    @Data
    public static class Key {
        private String key;
        private String secret;
    }
}
