package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.CalibrateDeviceDTO;
import com.irrigation_system.iot.dto.CreateDeviceDTO;
import com.irrigation_system.iot.dto.DeviceControlDTO;
import com.irrigation_system.iot.dto.DeviceDTO;
import com.irrigation_system.iot.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/devices")
@RequiredArgsConstructor
public class AdminDeviceController {

    private final DeviceService deviceService;

    @GetMapping
    @PreAuthorize("hasAuthority('DEVICE_READ_ALL')")
    public ResponseEntity<ApiResponse<Page<DeviceDTO>>> getAllDevices(
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<DeviceDTO> devices = deviceService.getAllDevices(pageable);
        
        ApiResponse<Page<DeviceDTO>> response = ApiResponse.<Page<DeviceDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Devices retrieved successfully")
                .data(devices)
                .build();
                
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEVICE_DELETE')")
    public ResponseEntity<Void> deleteDevice(@PathVariable String id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/calibrate")
    @PreAuthorize("hasAuthority('DEVICE_CALIBRATE')")
    public ResponseEntity<ApiResponse<DeviceDTO>> calibrateDevice(
            @PathVariable String id,
            @Valid @RequestBody CalibrateDeviceDTO calibrateDto) {
        
        DeviceDTO calibratedDevice = deviceService.calibrateDevice(id, calibrateDto);

        ApiResponse<DeviceDTO> response = ApiResponse.<DeviceDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Device calibrated successfully")
                .data(calibratedDevice)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEVICE_READ_ALL')")
    public ResponseEntity<ApiResponse<DeviceDTO>> getDeviceDetail(@PathVariable String id) {
        DeviceDTO device = deviceService.getAdminDeviceDetail(id);
        ApiResponse<DeviceDTO> response = ApiResponse.<DeviceDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Device details retrieved successfully")
                .data(device)
                .build();
        return ResponseEntity.ok(response);
    }
}

