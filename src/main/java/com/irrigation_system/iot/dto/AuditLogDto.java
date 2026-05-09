package com.irrigation_system.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {
    private String id;
    private String userId;
    private String username;
    private String action;
    private String targetId;
    private String payload;
    private LocalDateTime createdAt;
}
