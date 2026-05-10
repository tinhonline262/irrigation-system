package com.irrigation_system.iot.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.time.Instant;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WateringLogDTO {
    String id;
    String deviceId;
    String triggeredBy;
    String triggerType;
    Instant startedAt;
    Instant endedAt;
    Float waterAmountMl;
}
