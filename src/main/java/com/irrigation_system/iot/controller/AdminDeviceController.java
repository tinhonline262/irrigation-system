package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.CreateDeviceDTO;
import com.irrigation_system.iot.dto.DeviceDTO;
import com.irrigation_system.iot.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/devices")
@RequiredArgsConstructor
public class AdminDeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public ResponseEntity<ApiResponse<DeviceDTO>> createDevice(@Valid @RequestBody CreateDeviceDTO createDeviceDTO) {
        DeviceDTO createdDevice = deviceService.createDevice(createDeviceDTO);

        ApiResponse<DeviceDTO> response = ApiResponse.<DeviceDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("Device registered successfully")
                .data(createdDevice)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
