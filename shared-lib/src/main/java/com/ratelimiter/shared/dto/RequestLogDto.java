package com.ratelimiter.shared.dto;

import com.ratelimiter.shared.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestLogDto {
    private String ipAddress;
    private LocalDateTime timestamp;
    private RequestStatus status;
    private String path;
}