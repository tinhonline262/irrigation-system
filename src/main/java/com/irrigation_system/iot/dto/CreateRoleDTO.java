package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateRoleDTO {
    @NotBlank
    private String name;
    @NotNull
    private List<String> permissions;
}