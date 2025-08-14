package com.ratelimiter.gateway_service.client;

import com.ratelimiter.shared.dto.RequestLogDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class HttpMonitoringClient implements MonitoringClient {

    private final RestTemplate restTemplate;

    @Value("${monitoring-service.url}")
    private String monitoringServiceUrl;

    public HttpMonitoringClient() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void logRequest(RequestLogDto requestLogDto) {
        try {
            // Send the DTO to the monitoring service's logging endpoint
            restTemplate.postForObject(monitoringServiceUrl + "/logs", requestLogDto, Void.class);
            log.debug("Successfully sent request log to monitoring service for IP: {}", requestLogDto.getIpAddress());
        } catch (Exception e) {
            // Log an error but don't crash the gateway if the monitoring service is down
            log.error("Failed to send request log to monitoring service. Error: {}", e.getMessage());
        }
    }
}