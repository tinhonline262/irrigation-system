package com.irrigation_system.iot.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.time.Instant;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WateringStatusDTO {
    String deviceId;
    boolean running;
    String wateringLogId;
    String triggerType;
    Instant startedAt;
    Long elapsedSeconds;
}
