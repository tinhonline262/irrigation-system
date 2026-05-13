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
public class DashboardSummaryDTO {
    String deviceId;
    String status;
    Boolean statusRelay;
    Float latestSoilMoisturePercent;
    Float latestTemperatureCelsius;
    Float latestHumidityPercent;
    Float totalWaterAmountMlToday;
}
