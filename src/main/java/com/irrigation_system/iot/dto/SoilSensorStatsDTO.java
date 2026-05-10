package com.irrigation_system.iot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SoilSensorStatsDTO {
    PeriodStats last24Hours;
    PeriodStats last7Days;
    PeriodStats last30Days;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PeriodStats {
        Float minMoisturePercent;
        Float maxMoisturePercent;
        Float avgMoisturePercent;
    }
}