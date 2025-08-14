package com.ratelimiter.monitoring.entity;

import com.ratelimiter.shared.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ✅ 1. Import java.time.Instant instead of LocalDateTime
import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ipAddress;
    private String path;

    // ✅ 2. Change the field type from LocalDateTime to Instant
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}