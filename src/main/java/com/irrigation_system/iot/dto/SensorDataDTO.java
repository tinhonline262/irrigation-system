package com.irrigation_system.iot.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class SensorDataDTO {
    private String chipId;
    private Double temperature;
    private Double humidity;
    private Double soilMoisture;
    private Boolean relay;
    private Instant timestamp;
}
