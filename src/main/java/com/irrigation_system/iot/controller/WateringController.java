package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.DeviceControlDTO;
import com.irrigation_system.iot.dto.StopWateringRequest;
import com.irrigation_system.iot.dto.WateringLogDTO;
import com.irrigation_system.iot.dto.WateringLogPageDTO;
import com.irrigation_system.iot.dto.WateringLogStatsDTO;
import com.irrigation_system.iot.dto.WateringStatusDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.queue.producer.DeviceControlProducer;
import com.irrigation_system.iot.service.WateringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/my/devices")
@RequiredArgsConstructor
public class WateringController {

    private final WateringService wateringService;
    private final DeviceControlProducer deviceControlProducer;

    @PostMapping("/{deviceId}/water/start")
    public ResponseEntity<ApiResponse<WateringLogDTO>> startManualWatering(@PathVariable String deviceId) {
        WateringLogDTO wateringLog = wateringService.startManualWatering(deviceId);
        Device device = wateringService.verifyDeviceOwnership(deviceId).device;
        deviceControlProducer.sendControlCommand(device.getChipId(), "ON");
        return ResponseEntity.ok(ApiResponse.success(200, "Manual watering started", wateringLog));
    }

    @PostMapping("/{deviceId}/water/stop")
    public ResponseEntity<ApiResponse<WateringLogDTO>> stopManualWatering(@PathVariable String deviceId) {
        WateringLogDTO wateringLog = wateringService.stopManualWatering(deviceId);
        Device device = wateringService.verifyDeviceOwnership(deviceId).device;
        deviceControlProducer.sendControlCommand(device.getChipId(), "OFF");
        return ResponseEntity.ok(ApiResponse.success(200, "Manual watering stopped", wateringLog));
    }

    @PostMapping("/{deviceId}/control")
    public ResponseEntity<ApiResponse<Void>> controlDevice(
            @PathVariable String deviceId,
            @Valid @RequestBody DeviceControlDTO request) {
        Device device = wateringService.verifyDeviceOwnership(deviceId).device;
        deviceControlProducer.sendControlCommand(device.getChipId(), request.getCommand());
        return ResponseEntity.ok(ApiResponse.success(200, "Device control command sent", null));
    }

    @GetMapping("/{deviceId}/water/status")
    public ResponseEntity<ApiResponse<WateringStatusDTO>> getWateringStatus(@PathVariable String deviceId) {
        WateringStatusDTO status = wateringService.getWateringStatus(deviceId);
        return ResponseEntity.ok(ApiResponse.success(200, "Watering status fetched", status));
    }

    @GetMapping("/{deviceId}/water/logs")
    public ResponseEntity<ApiResponse<WateringLogPageDTO>> getWateringLogs(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        WateringLogPageDTO logPage = wateringService.getWateringLogs(deviceId, page, size);
        return ResponseEntity.ok(ApiResponse.success(200, "Watering logs fetched", logPage));
    }

    @GetMapping("/{deviceId}/water/logs/export")
    public ResponseEntity<StreamingResponseBody> exportWateringLogsCsv(@PathVariable String deviceId) {
        StreamingResponseBody responseBody = outputStream -> wateringService.exportWateringLogsCsv(deviceId, outputStream);
        String filename = String.format("watering-logs-%s.csv", deviceId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(responseBody);
    }

    @GetMapping("/{deviceId}/water/logs/stats")
    public ResponseEntity<ApiResponse<List<WateringLogStatsDTO>>> getWateringLogStats(@PathVariable String deviceId) {
        List<WateringLogStatsDTO> stats = wateringService.getWateringLogStats(deviceId);
        return ResponseEntity.ok(ApiResponse.success(200, "Watering log stats fetched", stats));
    }
}
