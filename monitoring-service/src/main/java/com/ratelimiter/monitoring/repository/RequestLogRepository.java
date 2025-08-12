package com.ratelimiter.monitoring.repository;

import com.ratelimiter.monitoring.entity.RequestLog;
import com.ratelimiter.shared.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {

    long countByStatus(RequestStatus status);

}