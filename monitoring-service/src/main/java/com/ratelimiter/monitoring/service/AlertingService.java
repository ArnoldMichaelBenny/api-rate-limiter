package com.ratelimiter.monitoring.service;

import com.ratelimiter.monitoring.repository.RequestLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertingService {

    private final RequestLogRepository requestLogRepository;
    private static final double ALERT_THRESHOLD = 0.90; // Alert if failure rate is > 90%
    private static final int MINIMUM_REQUESTS_FOR_ALERT = 10; // Only alert if there are at least 10 requests

    /**
     * Runs every minute to check for anomalous request patterns.
     */
    @Scheduled(fixedRate = 60000) // Run every 60,000 milliseconds (1 minute)
    public void checkForAnomalies() {
        log.debug("Running anomaly detection task...");
        Instant fiveMinutesAgo = Instant.now().minus(5, ChronoUnit.MINUTES);

        // Get failure rates for all IPs in the last 5 minutes
        List<Map<String, Object>> failureRates = requestLogRepository.findFailureRatesByIpAddress(fiveMinutesAgo);

        failureRates.forEach(rateInfo -> {
            String ipAddress = (String) rateInfo.get("ipAddress");
            long totalRequests = (long) rateInfo.get("totalRequests");
            double failureRate = (double) rateInfo.get("failureRate");

            // Check if the failure rate exceeds our threshold
            if (totalRequests >= MINIMUM_REQUESTS_FOR_ALERT && failureRate > ALERT_THRESHOLD) {
                log.warn("CRITICAL ALERT: High failure rate detected for IP [{}]. Failure rate is {}% over the last 5 minutes ({} total requests).",
                        ipAddress, String.format("%.2f", failureRate * 100), totalRequests);
            }
        });
    }
}