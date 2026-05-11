package com.irrigation_system.iot.dto;

import lombok.Data;

@Data
public class DeviceRegisterDTO {
    private String chipId;
    private String firmware; // optional
}