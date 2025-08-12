package com.ratelimiter.gateway_service.interceptor;

import com.ratelimiter.gateway_service.service.MonitoringClient;
import com.ratelimiter.shared.dto.RequestLogDto;
import com.ratelimiter.shared.enums.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimiterInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final MonitoringClient monitoringClient;

    @Value("${rate-limiter.requests-per-minute}")
    private int requestsPerMinute;

    private static final String REQUEST_STATUS_ATTR = "requestStatus";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        long currentMinute = Instant.now().getEpochSecond() / 60;
        String key = "rate_limit:" + clientIp + ":" + currentMinute;

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null) {
            // Set expiry only for the first request in the window
            if (count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(61)); // A little over 60s to be safe
            }

            if (count > requestsPerMinute) {
                log.warn("Rate limit exceeded for IP: {}. Count: {}", clientIp, count);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded");
                request.setAttribute(REQUEST_STATUS_ATTR, RequestStatus.BLOCKED);
                return false;
            }
        }

        log.info("Request allowed for IP: {}. Count: {}", clientIp, count);
        request.setAttribute(REQUEST_STATUS_ATTR, RequestStatus.ALLOWED);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestStatus status = (RequestStatus) request.getAttribute(REQUEST_STATUS_ATTR);

        // Ensure we log even if preHandle fails to set the attribute for some reason
        if (status == null) {
            status = response.getStatus() == HttpStatus.TOO_MANY_REQUESTS.value() ? RequestStatus.BLOCKED : RequestStatus.ALLOWED;
        }

        RequestLogDto logDto = new RequestLogDto(
                request.getRemoteAddr(),
                LocalDateTime.now(),
                status,
                request.getRequestURI()
        );
        monitoringClient.logRequest(logDto);
    }
}