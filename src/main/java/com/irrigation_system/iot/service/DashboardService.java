package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.*;
import java.time.Instant;
import java.util.List;

public interface DashboardService {
    DashboardSummaryDTO getDashboardSummary(String deviceId);
    DashboardSummaryDTO getDashboardSummaryInternal(String deviceId);
    SoilSensorReadingDTO getLatestSoilSensorReading(String deviceId);
    List<SoilSensorHistoryDTO> getSoilSensorHistory(String deviceId, Instant startDate, Instant endDate, String interval);
    AirSensorReadingDTO getLatestAirSensorReading(String deviceId);
    List<AirSensorHistoryDTO> getAirSensorHistory(String deviceId, Instant startDate, Instant endDate, String interval);
    SoilSensorStatsDTO getSoilSensorStats(String deviceId);
}
