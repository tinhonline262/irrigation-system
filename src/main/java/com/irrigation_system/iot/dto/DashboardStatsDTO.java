package com.irrigation_system.iot.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalUsers;
    private long totalDevices;
    private long onlineDevices;
    private long offlineDevices;
}
