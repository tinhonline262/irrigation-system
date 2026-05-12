package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.AirSensorHistoryDTO;
import com.irrigation_system.iot.dto.AirSensorReadingDTO;
import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.DashboardSummaryDTO;
import com.irrigation_system.iot.dto.SoilSensorHistoryDTO;
import com.irrigation_system.iot.dto.SoilSensorReadingDTO;
import com.irrigation_system.iot.dto.SoilSensorStatsDTO;
import com.irrigation_system.iot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/my/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DashboardService dashboardService;

    @GetMapping("/{deviceId}/soil/latest")
    public ResponseEntity<ApiResponse<SoilSensorReadingDTO>> getLatestSoilSensorReading(@PathVariable String deviceId) {
        SoilSensorReadingDTO reading = dashboardService.getLatestSoilSensorReading(deviceId);
        return ResponseEntity.ok(ApiResponse.success(200, "Latest soil sensor reading fetched", reading));
    }

    @GetMapping("/{deviceId}/soil/history")
    public ResponseEntity<ApiResponse<List<SoilSensorHistoryDTO>>> getSoilSensorHistory(
            @PathVariable String deviceId,
            @RequestParam Instant startDate,
            @RequestParam Instant endDate,
            @RequestParam(defaultValue = "RAW") String interval) {
        List<SoilSensorHistoryDTO> history = dashboardService.getSoilSensorHistory(deviceId, startDate, endDate, interval);
        return ResponseEntity.ok(ApiResponse.success(200, "Soil sensor history fetched", history));
    }

    @GetMapping("/{deviceId}/soil/stats")
    public ResponseEntity<ApiResponse<SoilSensorStatsDTO>> getSoilSensorStats(@PathVariable String deviceId) {
        SoilSensorStatsDTO stats = dashboardService.getSoilSensorStats(deviceId);
        return ResponseEntity.ok(ApiResponse.success(200, "Soil sensor stats fetched", stats));
    }

    @GetMapping("/{deviceId}/air/latest")
    public ResponseEntity<ApiResponse<AirSensorReadingDTO>> getLatestAirSensorReading(@PathVariable String deviceId) {
        AirSensorReadingDTO reading = dashboardService.getLatestAirSensorReading(deviceId);
        return ResponseEntity.ok(ApiResponse.success(200, "Latest air sensor reading fetched", reading));
    }

    @GetMapping("/{deviceId}/air/history")
    public ResponseEntity<ApiResponse<List<AirSensorHistoryDTO>>> getAirSensorHistory(
            @PathVariable String deviceId,
            @RequestParam Instant startDate,
            @RequestParam Instant endDate,
            @RequestParam(defaultValue = "RAW") String interval) {
        List<AirSensorHistoryDTO> history = dashboardService.getAirSensorHistory(deviceId, startDate, endDate, interval);
        return ResponseEntity.ok(ApiResponse.success(200, "Air sensor history fetched", history));
    }
}
