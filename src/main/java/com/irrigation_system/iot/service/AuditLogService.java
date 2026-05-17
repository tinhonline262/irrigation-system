package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.AuditLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AuditLogService {
    Page<AuditLogDto> getAuditLogs(String userId, LocalDateTime from, LocalDateTime to, Pageable pageable);
    void logAction(String action, String targetId, String payload);
    void logAction(String action, String targetId, Object payloadObject);
}
