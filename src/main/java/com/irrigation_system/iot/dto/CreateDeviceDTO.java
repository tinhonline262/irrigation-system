package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDeviceDTO {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Device name is required")
    private String name;
}
