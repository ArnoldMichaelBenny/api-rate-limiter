package com.ratelimiter.gateway_service.service;

import com.ratelimiter.gateway_service.dto.RequestLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringClient {

    private final RestTemplate restTemplate;

    @Value("${monitoring-service.url}")
    private String monitoringServiceUrl;

    @Async
    public void logRequest(RequestLog requestLog) {
        restTemplate.postForObject(monitoringServiceUrl + "/log", requestLog, Void.class);
    }
}