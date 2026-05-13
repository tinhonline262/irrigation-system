package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ToggleScheduleDTO {
    @NotNull(message = "Enabled is required")
    private Boolean enabled;
}

