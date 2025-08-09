package com.ratelimiter.monitoringservice.repository;

import com.ratelimiter.monitoringservice.model.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {

    long countByClientId(String clientId);
}
