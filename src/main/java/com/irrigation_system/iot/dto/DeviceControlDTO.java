package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DeviceControlDTO {

    @NotNull(message = "Command is required")
    @Pattern(regexp = "^(ON|OFF)$", message = "Command must be ON or OFF")
    private String command;
}
