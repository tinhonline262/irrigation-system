package com.irrigation_system.iot.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class DeviceDTO {
    private String id;
    private String name;
    private String userId;
    private String username;
    private String ownerId;
    private String status;
    private Boolean statusDelay;
    private Boolean autoWaterEnabled;
    private Float moistureThresholdLow;
    private Float moistureThresholdHigh;
    private Float soilMoistureOffset;
    private Float airTemperatureOffset;
    private Float airHumidityOffset;
}
