package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.DashboardSummaryDTO;
import com.irrigation_system.iot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/{deviceId}/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary(@PathVariable String deviceId) {
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary(deviceId);
        return ResponseEntity.ok(ApiResponse.success(200, "Dashboard summary fetched", summary));
    }
}
