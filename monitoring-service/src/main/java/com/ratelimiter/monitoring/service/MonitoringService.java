package com.ratelimiter.monitoring.service;

import com.ratelimiter.monitoring.entity.RequestLog;
import com.ratelimiter.monitoring.repository.RequestLogRepository;
import com.ratelimiter.shared.RateLimitEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final RequestLogRepository requestLogRepository;

    @Transactional
    public void processAndSaveEvent(RateLimitEvent event) {
        log.info("Processing rate limit event for client [{}]: {}", event.getClientKey(), event.isAllowed() ? "ALLOWED" : "BLOCKED");
        RequestLog logEntry = RequestLog.builder()
                .clientKey(event.getClientKey())
                .endpoint(event.getEndpoint())
                .timestamp(event.getTimestamp())
                .algorithm(event.getAlgorithm())
                .allowed(event.isAllowed())
                .build();

        requestLogRepository.save(logEntry);
        log.debug("Saved request log to database: {}", logEntry.getId());
    }
}