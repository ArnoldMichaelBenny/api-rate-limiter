package com.ratelimiter.gateway_service.client;

import com.ratelimiter.shared.dto.RequestLogDto;

// ✅ Updated to accept the full DTO
public interface MonitoringClient {
    void logRequest(RequestLogDto requestLogDto);
}