package com.irrigation_system.iot.dto;

import lombok.Data;
import java.util.List;

@Data
public class RoleDTO {
    private String id;
    private String name;
    private List<String> permissions;
}