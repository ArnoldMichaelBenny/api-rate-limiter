package com.ratelimiter.monitoring.controller;

import com.ratelimiter.monitoring.service.MonitoringService;
import com.ratelimiter.shared.RateLimitEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    /**
     * Receives rate limit events from the Gateway service via WebSocket.
     * It persists the event and then broadcasts it to all subscribed frontend clients.
     */
    @MessageMapping("/rate-limit-event") // Listens for messages on "/app/rate-limit-event"
    @SendTo("/topic/rate-limit-events")  // Broadcasts the return value to this topic
    public RateLimitEvent handleRateLimitEvent(RateLimitEvent event) {
        monitoringService.processAndSaveEvent(event);
        return event;
    }
}