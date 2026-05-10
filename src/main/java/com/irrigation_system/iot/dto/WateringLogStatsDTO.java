package com.irrigation_system.iot.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WateringLogStatsDTO {
    String date;
    Float totalWaterAmountMl;
    Long wateringCount;
}
