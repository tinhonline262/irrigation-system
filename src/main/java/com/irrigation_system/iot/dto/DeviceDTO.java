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
    private String chipId;
    private Instant claimedAt;
    private Instant lastSeenAt;
    private Boolean statusRelay;
    private Boolean autoWaterEnabled;
    private Float moistureThresholdLow;
    private Float moistureThresholdHigh;
    private Float soilMoistureOffset;
    private Float airTemperatureOffset;
    private Float airHumidityOffset;
    private Integer wifiRssi;
    private String ip;
    private Long freeHeap;
    private Long uptime;
}
