package com.ratelimiter.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDto {
    private long totalRequests;
    private long allowedRequests;
    private long blockedRequests;
}