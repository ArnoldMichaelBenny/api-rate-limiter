package com.ratelimiter.shared.dto;

import com.ratelimiter.shared.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 1. Import java.time.Instant
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestLogDto {
    private String ipAddress;
    // 2. This is the required change
    private Instant timestamp;
    private RequestStatus status;
    private String path;
}