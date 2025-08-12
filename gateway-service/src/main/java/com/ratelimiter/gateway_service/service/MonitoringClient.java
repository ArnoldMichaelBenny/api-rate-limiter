package com.ratelimiter.gateway_service.service;

import com.ratelimiter.shared.dto.RequestLogDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringClient {

    private final RestTemplate restTemplate;

    @Value("${monitoring-service.url}")
    private String monitoringServiceUrl;

    @Value("${monitoring-service.log-endpoint}")
    private String logEndpoint;

    @Async
    public void logRequest(RequestLogDto requestLogDto) {
        try {
            restTemplate.postForObject(monitoringServiceUrl + logEndpoint, requestLogDto, Void.class);
            log.debug("Successfully sent log to monitoring service for IP: {}", requestLogDto.getIpAddress());
        } catch (RestClientException e) {
            log.error("Failed to send log to monitoring service. Error: {}", e.getMessage());
        }
    }
}