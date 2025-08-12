package com.ratelimiter.gateway_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "api")
@Getter
@Setter
public class ApiKeyProperties {

    private List<Key> keys = new ArrayList<>();

    @Getter
    @Setter
    public static class Key {
        private String key;
        private String secret;
    }
}