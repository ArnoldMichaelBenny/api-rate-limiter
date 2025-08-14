package com.ratelimiter.monitoring.service;

import com.ratelimiter.monitoring.dto.AnalyticsDto;
import com.ratelimiter.monitoring.entity.RequestLog;
import com.ratelimiter.monitoring.repository.RequestLogRepository;
import com.ratelimiter.shared.dto.RequestLogDto;
import com.ratelimiter.shared.enums.RequestStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final RequestLogRepository requestLogRepository;
    // ✅ 1. Inject the WebSocket messaging template
    private final SimpMessagingTemplate messagingTemplate;

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

        // ✅ 2. After saving, broadcast the new analytics data
        broadcastAnalyticsUpdate();
    }

    public AnalyticsDto getAnalytics() {
        long allowed = requestLogRepository.countByStatus(RequestStatus.ALLOWED);
        long blocked = requestLogRepository.countByStatus(RequestStatus.BLOCKED);
        long total = allowed + blocked;

        return new AnalyticsDto(total, allowed, blocked);
    }

    /**
     * ✅ 3. New private method to calculate and broadcast the update.
     * This sends the latest AnalyticsDto to all clients subscribed to "/topic/analytics".
     */
    private void broadcastAnalyticsUpdate() {
        AnalyticsDto analytics = getAnalytics();
        log.info("Broadcasting analytics update: Total={}, Allowed={}, Blocked={}",
                analytics.getTotalRequests(), analytics.getAllowedRequests(), analytics.getBlockedRequests());
        messagingTemplate.convertAndSend("/topic/analytics", analytics);
    }
}