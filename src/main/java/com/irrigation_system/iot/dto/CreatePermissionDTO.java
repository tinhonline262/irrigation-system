package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePermissionDTO {
    @NotBlank(message = "Permission name is required")
    private String name;
    
    private String description;
}