package com.ratelimiter.monitoringservice.controller;

import com.ratelimiter.monitoringservice.model.RequestLog;
import com.ratelimiter.monitoringservice.service.RequestLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AnalyticsController {

    private final RequestLogService requestLogService;

    @GetMapping("/")
    public String home() {
        return "Monitoring Service is running.";
    }

    @PostMapping("/log")
    public void logRequest(@RequestBody RequestLog requestLog) {
        requestLogService.saveRequestLog(requestLog);
    }

    @GetMapping("/analytics/{clientId}")
    public long getRequestCountByClientId(@PathVariable String clientId) {
        return requestLogService.getRequestCountByClientId(clientId);
    }
}
