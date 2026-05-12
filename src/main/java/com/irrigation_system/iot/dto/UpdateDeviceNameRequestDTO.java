package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateDeviceNameRequestDTO {

    @NotBlank(message = "name is required")
    private String name;
}
