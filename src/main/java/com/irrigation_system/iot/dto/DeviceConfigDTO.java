package com.irrigation_system.iot.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class DeviceConfigDTO {
    private String id;
    private String name;
    private String status;
    private Float moistureThresholdLow;
    private Float moistureThresholdHigh;
    private Boolean autoWaterEnabled;
    private Instant lastSeenAt;
    private Float soilMoistureOffset;
    private Float airTemperatureOffset;
    private Float airHumidityOffset;
}