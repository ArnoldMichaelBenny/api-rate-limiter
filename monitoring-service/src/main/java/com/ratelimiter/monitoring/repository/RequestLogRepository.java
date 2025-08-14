package com.ratelimiter.monitoring.repository;

import com.ratelimiter.monitoring.entity.RequestLog;
import com.ratelimiter.shared.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Long> {

    long countByStatus(RequestStatus status);

    /**
     * ✅ New method for the AlertingService.
     * Calculates the failure rate for each IP address since a given timestamp.
     * The failure rate is the ratio of BLOCKED requests to the total number of requests.
     * Returns a list of maps, each containing the ipAddress, totalRequests, and failureRate.
     */
    @Query("SELECT new map(r.ipAddress as ipAddress, COUNT(r.id) as totalRequests, " +
            "SUM(CASE WHEN r.status = 'BLOCKED' THEN 1.0 ELSE 0.0 END) / COUNT(r.id) as failureRate) " +
            "FROM RequestLog r " +
            "WHERE r.timestamp >= :since " +
            "GROUP BY r.ipAddress")
    List<Map<String, Object>> findFailureRatesByIpAddress(@Param("since") Instant since);
}