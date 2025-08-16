package com.ratelimiter.gateway_service.client;

import com.ratelimiter.shared.dto.RequestLogDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    /**
     * This method is now protected by a circuit breaker.
     * The 'name' must match the instance name in application.yml.
     * If this method fails repeatedly, the circuit will open, and the 'fallbackMethod' will be called instead.
     */
    @Override
    @CircuitBreaker(name = "monitoring-service", fallbackMethod = "logRequestFallback")
    public void logRequest(RequestLogDto requestLogDto) {
        // We no longer need a try-catch block here. Resilience4j will handle exceptions.
        restTemplate.postForObject(monitoringServiceUrl + "/logs", requestLogDto, Void.class);
        log.debug("Successfully sent request log to monitoring service for IP: {}", requestLogDto.getIpAddress());
    }

    /**
     * This is the fallback method. It is executed when the circuit breaker is open.
     * It must have the same signature as the original method, with an additional Throwable parameter at the end.
     * This ensures the gateway remains operational even if the monitoring service is down.
     */
    public void logRequestFallback(RequestLogDto requestLogDto, Throwable t) {
        log.warn("Monitoring service is unavailable. Circuit breaker is open. Skipping log for IP: {}. Reason: {}",
                requestLogDto.getIpAddress(), t.getMessage());
    }
}
