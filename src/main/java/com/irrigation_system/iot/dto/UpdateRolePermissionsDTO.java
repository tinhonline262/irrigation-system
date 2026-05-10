package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class UpdateRolePermissionsDTO {
    @NotNull
    private List<String> permissions;
}