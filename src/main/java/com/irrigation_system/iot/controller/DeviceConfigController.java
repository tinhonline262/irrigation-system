package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.DeviceConfigDTO;
import com.irrigation_system.iot.dto.UpdateDeviceConfigDTO;
import com.irrigation_system.iot.service.DeviceConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/my/devices/{deviceId}/config")
@RequiredArgsConstructor
public class DeviceConfigController {

    private final DeviceConfigService deviceConfigService;

    /**
     * GET /api/v1/devices/{deviceId}/config
     * Lấy ngưỡng độ ẩm và chế độ tự động của thiết bị.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('DEVICE_CONFIG_READ')")
    public ResponseEntity<ApiResponse<DeviceConfigDTO>> getDeviceConfig(
            @PathVariable String deviceId) {

        DeviceConfigDTO config = deviceConfigService.getDeviceConfig(deviceId);

        ApiResponse<DeviceConfigDTO> response = ApiResponse.<DeviceConfigDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Device config retrieved successfully")
                .data(config)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/v1/devices/{deviceId}/config
     * Cập nhật ngưỡng độ ẩm và bật/tắt auto water.
     */
    @PutMapping
    @PreAuthorize("hasAuthority('DEVICE_CONFIG_UPDATE')")
    public ResponseEntity<ApiResponse<DeviceConfigDTO>> updateDeviceConfig(
            @PathVariable String deviceId,
            @Valid @RequestBody UpdateDeviceConfigDTO updateDeviceConfigDTO) {

        DeviceConfigDTO updated = deviceConfigService.updateDeviceConfig(deviceId, updateDeviceConfigDTO);

        ApiResponse<DeviceConfigDTO> response = ApiResponse.<DeviceConfigDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Device config updated successfully")
                .data(updated)
                .build();

        return ResponseEntity.ok(response);
    }
}