package com.irrigation_system.iot.dto;

import lombok.Data;

@Data
public class CalibrateDeviceDTO {
    private Float soilMoistureOffset;
    private Float airTemperatureOffset;
    private Float airHumidityOffset;
}