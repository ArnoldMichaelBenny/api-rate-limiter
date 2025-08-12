package com.ratelimiter.monitoring.service;

import com.ratelimiter.monitoring.dto.AnalyticsDto;
import com.ratelimiter.monitoring.entity.RequestLog;
import com.ratelimiter.monitoring.repository.RequestLogRepository;
import com.ratelimiter.shared.dto.RequestLogDto;
import com.ratelimiter.shared.enums.RequestStatus;
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
    public void saveRequestLog(RequestLogDto requestLogDto) {
        log.info("Processing request log for IP [{}]: Status {}", requestLogDto.getIpAddress(), requestLogDto.getStatus());
        RequestLog logEntry = RequestLog.builder()
                .ipAddress(requestLogDto.getIpAddress())
                .path(requestLogDto.getPath())
                .timestamp(requestLogDto.getTimestamp())
                .status(requestLogDto.getStatus())
                .build();

        requestLogRepository.save(logEntry);
        log.debug("Saved request log to database: {}", logEntry.getId());
    }

    public AnalyticsDto getAnalytics() {
        long allowed = requestLogRepository.countByStatus(RequestStatus.ALLOWED);
        long blocked = requestLogRepository.countByStatus(RequestStatus.BLOCKED);
        long total = allowed + blocked;

        return new AnalyticsDto(total, allowed, blocked);
    }
}