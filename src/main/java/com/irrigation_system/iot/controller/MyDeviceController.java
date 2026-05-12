package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.ClaimDeviceRequestDTO;
import com.irrigation_system.iot.dto.DeviceDTO;
import com.irrigation_system.iot.dto.UpdateDeviceNameRequestDTO;
import com.irrigation_system.iot.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/my/devices")
@RequiredArgsConstructor
public class MyDeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceDTO>>> getMyDevices() {
        List<DeviceDTO> devices = deviceService.getMyDevices();
        return ResponseEntity.ok(ApiResponse.<List<DeviceDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Successfully retrieved user devices")
                .data(devices)
                .build());
    }

    @PostMapping("/claim")
    public ResponseEntity<ApiResponse<DeviceDTO>> claimDevice(
            @Valid @RequestBody ClaimDeviceRequestDTO request) {
        DeviceDTO claimed = deviceService.claimDevice(request.getChipId());
        return ResponseEntity.ok(ApiResponse.<DeviceDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Device claimed successfully")
                .data(claimed)
                .build());
    }

    @DeleteMapping("/{deviceId}/unclaim")
    public ResponseEntity<ApiResponse<Void>> unclaimDevice(@PathVariable String deviceId) {
        deviceService.unclaimDevice(deviceId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Device unclaimed successfully")
                .build());
    }

    @PatchMapping("/{deviceId}/name")
    public ResponseEntity<ApiResponse<DeviceDTO>> updateDeviceName(
            @PathVariable String deviceId,
            @Valid @RequestBody UpdateDeviceNameRequestDTO request) {
        DeviceDTO updated = deviceService.updateDeviceName(deviceId, request.getName());
        return ResponseEntity.ok(ApiResponse.<DeviceDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Device name updated successfully")
                .data(updated)
                .build());
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<DeviceDTO>> getMyDeviceDetail(@PathVariable String deviceId) {
        DeviceDTO device = deviceService.getMyDeviceDetail(deviceId);
        return ResponseEntity.ok(ApiResponse.<DeviceDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Device detail retrieved successfully")
                .data(device)
                .build());
    }
}