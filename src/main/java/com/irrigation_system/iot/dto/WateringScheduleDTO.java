package com.irrigation_system.iot.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class WateringScheduleDTO {
    private String id;
    private String deviceId;
    private String cronExpression;
    private Long durationInMinutes;
    private Boolean enabled;
    private Instant nextRunAt;
}

