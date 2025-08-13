package com.ratelimiter.gateway_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyConfig {
    private String apiKey;
    private String algorithm;
    private int limit;
}
