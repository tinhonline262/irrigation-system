package com.irrigation_system.iot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irrigation_system.iot.dto.AuditLogDto;
import com.irrigation_system.iot.entity.AuditLog;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.mapper.AuditLogMapper;
import com.irrigation_system.iot.repository.AuditLogRepository;
import com.irrigation_system.iot.repository.UserRepository;
import com.irrigation_system.iot.service.AuditLogService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Page<AuditLogDto> getAuditLogs(String userId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null && !userId.isBlank()) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return auditLogRepository.findAll(spec, pageable).map(auditLogMapper::toDto);
    }

    @Override
    public void logAction(String action, String targetId, String payload) {
        UserEntity currentUser = getCurrentUser();
        if (currentUser == null) {
            log.warn("Could not log action {} target {} as no user is authenticated", action, targetId);
            return;
        }

        AuditLog auditLog = AuditLog.builder()
                .user(currentUser)
                .action(action)
                .targetId(targetId)
                .payload(payload)
                .build();
        auditLogRepository.save(auditLog);
    }

    /**
     * Log action with object payload that will be serialized to JSON
     */
    @Override
    public void logAction(String action, String targetId, Object payloadObject) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payloadObject);
            logAction(action, targetId, jsonPayload);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit log payload to JSON", e);
            logAction(action, targetId, "{}");
        }
    }

    private UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }
}
