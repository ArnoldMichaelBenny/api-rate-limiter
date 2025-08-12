package com.ratelimiter.monitoring.controller;

import com.ratelimiter.monitoring.service.MonitoringService;
import com.ratelimiter.shared.dto.RequestLogDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringService monitoringService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void logRequest(@RequestBody RequestLogDto requestLogDto) {
        monitoringService.saveRequestLog(requestLogDto);
    }
}