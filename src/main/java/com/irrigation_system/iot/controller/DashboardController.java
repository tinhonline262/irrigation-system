package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.DashboardSummaryDTO;
import com.irrigation_system.iot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.irrigation_system.iot.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/my/devices")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // REST API endpoint for fetching dashboard summary
    @GetMapping("/{deviceId}/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary(@PathVariable String deviceId) {
        DashboardSummaryDTO summary = dashboardService.getDashboardSummary(deviceId);
        return ResponseEntity.ok(ApiResponse.success(200, "Dashboard summary fetched", summary));
    }

    // WebSocket endpoint for realtime dashboard subscription
    @MessageMapping("/dashboard/{deviceId}")
    @SendTo("/topic/dashboard/{deviceId}")
    public DashboardSummaryDTO subscribeToDashboard(@DestinationVariable String deviceId) {
        return dashboardService.getDashboardSummary(deviceId);
    }
}
