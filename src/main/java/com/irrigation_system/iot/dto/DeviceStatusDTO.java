package com.irrigation_system.iot.dto;

import lombok.Data;

@Data
public class DeviceStatusDTO {
    private String chipId;
    private String status;
    private Boolean relay;
    private Integer wifiRssi;
    private String ip;
    private Long freeHeap;
    private Long uptime;
}