package com.irrigation_system.iot.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SensorDataDTO {
    private String deviceId;
    private Double temperature;
    private Double humidity;
    private Double soilMoisture;
    private LocalDateTime timestamp;
}
