package com.irrigation_system.iot.dto;

import lombok.Data;

@Data
public class DeviceStatusDTO {
    private String chipId;
    private String status;
}