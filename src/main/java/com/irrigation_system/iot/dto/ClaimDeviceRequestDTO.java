package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClaimDeviceRequestDTO {

    @NotBlank(message = "chipId is required")
    private String chipId;
}
