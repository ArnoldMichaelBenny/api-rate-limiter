package com.ratelimiter.gateway_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestLog {
    private String clientId;
    private String endpoint;
    private Instant timestamp;
    private boolean allowed;
    private String httpMethod;
}
