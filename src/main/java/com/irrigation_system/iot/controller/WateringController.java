package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.StopWateringRequest;
import com.irrigation_system.iot.dto.WateringLogDTO;
import com.irrigation_system.iot.dto.WateringLogPageDTO;
import com.irrigation_system.iot.dto.WateringLogStatsDTO;
import com.irrigation_system.iot.dto.WateringStatusDTO;
import com.irrigation_system.iot.service.WateringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.StreamingResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class WateringController {

    private final WateringService wateringService;

    @PostMapping("/{deviceId}/water/start")
    public ResponseEntity<ApiResponse<WateringLogDTO>> startManualWatering(@PathVariable String deviceId) {
        WateringLogDTO wateringLog = wateringService.startManualWatering(deviceId);
        return ResponseEntity.ok(ApiResponse.success(200, "Manual watering started", wateringLog));
    }

    @PostMapping("/{deviceId}/water/stop")
    public ResponseEntity<ApiResponse<WateringLogDTO>> stopManualWatering(
            @PathVariable String deviceId,
            @Valid @RequestBody StopWateringRequest request) {
        WateringLogDTO wateringLog = wateringService.stopManualWatering(deviceId, request.getWaterAmountMl());
        return ResponseEntity.ok(ApiResponse.success(200, "Manual watering stopped", wateringLog));
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
                .contentType(MediaType.valueOf("text/csv"))
                .body(responseBody);
    }

    @GetMapping("/{deviceId}/water/logs/stats")
    public ResponseEntity<ApiResponse<List<WateringLogStatsDTO>>> getWateringLogStats(@PathVariable String deviceId) {
        List<WateringLogStatsDTO> stats = wateringService.getWateringLogStats(deviceId);
        return ResponseEntity.ok(ApiResponse.success(200, "Watering log stats fetched", stats));
    }
}
