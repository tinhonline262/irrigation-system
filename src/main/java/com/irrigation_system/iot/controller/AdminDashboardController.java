package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.DashboardStatsDTO;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN_DASHBOARD_READ')")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalDevices = deviceRepository.count();
        long onlineDevices = deviceRepository.findAll().stream()
                .filter(d -> "online".equalsIgnoreCase(d.getStatus()))
                .count();
        long offlineDevices = totalDevices - onlineDevices;

        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalDevices(totalDevices)
                .onlineDevices(onlineDevices)
                .offlineDevices(offlineDevices)
                .build();

        return ResponseEntity.ok(ApiResponse.<DashboardStatsDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Dashboard stats retrieved successfully")
                .data(stats)
                .build());
    }
}