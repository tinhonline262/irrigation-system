package com.irrigation_system.iot.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class NotificationDTO {
    private String id;
    private String userId;
    private String deviceId;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private Instant readAt;
    private Instant createdAt;
}