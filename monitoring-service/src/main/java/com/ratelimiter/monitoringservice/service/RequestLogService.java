package com.ratelimiter.monitoringservice.service;

import com.ratelimiter.monitoringservice.model.RequestLog;
import com.ratelimiter.monitoringservice.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestLogService {

    private final RequestLogRepository requestLogRepository;

    public void saveRequestLog(RequestLog requestLog) {
        requestLogRepository.save(requestLog);
    }

    public long getRequestCountByClientId(String clientId) {
        return requestLogRepository.countByClientId(clientId);
    }
}
