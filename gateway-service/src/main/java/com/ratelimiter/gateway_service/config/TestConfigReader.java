package com.ratelimiter.gateway_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TestConfigReader implements CommandLineRunner {

    // ✅ Read values from application.yml
    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${api.keys[0].key}")
    private String firstApiKey;

    @Override
    public void run(String... args) {
        System.out.println("===== CONFIG SANITY CHECK =====");
        System.out.println("Application Name: " + appName);
        System.out.println("Redis Host: " + redisHost);
        System.out.println("Redis Port: " + redisPort);
        System.out.println("First API Key: " + firstApiKey);
        System.out.println("===== CONFIG CHECK COMPLETE =====");
    }
}
