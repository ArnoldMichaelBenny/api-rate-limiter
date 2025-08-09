package com.ratelimiter.shared;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitEvent {
    private String clientKey;
    private String endpoint;
    private Instant timestamp;
    private String algorithm;
    private boolean allowed;
}