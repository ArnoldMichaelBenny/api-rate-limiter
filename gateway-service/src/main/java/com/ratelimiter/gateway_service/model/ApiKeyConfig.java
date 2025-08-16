package com.ratelimiter.gateway_service.model;

public class ApiKeyConfig {

    private String apiKey;
    private String algorithm;
    private int limit;
    private Integer burstLimit;
    private Integer refillRate;

    // ✅ No-args constructor
    public ApiKeyConfig() {
    }

    // ✅ 3-args constructor (used in your RateLimiterService)
    public ApiKeyConfig(String apiKey, String algorithm, int limit) {
        this(apiKey, algorithm, limit, null, null);
    }

    // ✅ 5-args constructor (full config)
    public ApiKeyConfig(String apiKey, String algorithm, int limit, Integer burstLimit, Integer refillRate) {
        this.apiKey = apiKey;
        this.algorithm = algorithm;
        this.limit = limit;
        this.burstLimit = burstLimit;
        this.refillRate = refillRate;
    }

    // Getters and setters
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Integer getBurstLimit() {
        return burstLimit;
    }

    public void setBurstLimit(Integer burstLimit) {
        this.burstLimit = burstLimit;
    }

    public Integer getRefillRate() {
        return refillRate;
    }

    public void setRefillRate(Integer refillRate) {
        this.refillRate = refillRate;
    }
}
