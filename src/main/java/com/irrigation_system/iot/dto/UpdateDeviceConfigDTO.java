package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class UpdateDeviceConfigDTO {

    @DecimalMin(value = "0.0", message = "Moisture threshold low must be >= 0")
    @DecimalMax(value = "100.0", message = "Moisture threshold low must be <= 100")
    private Float moistureThresholdLow;

    @DecimalMin(value = "0.0", message = "Moisture threshold high must be >= 0")
    @DecimalMax(value = "100.0", message = "Moisture threshold high must be <= 100")
    private Float moistureThresholdHigh;

    private Boolean autoWaterEnabled;
}