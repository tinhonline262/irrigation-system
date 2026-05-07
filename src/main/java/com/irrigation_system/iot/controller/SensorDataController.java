package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.SensorDataDTO;
import com.irrigation_system.iot.service.SensorDataProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sensors")
@RequiredArgsConstructor
public class SensorDataController {

    private final SensorDataProducer sensorDataProducer;

    @PostMapping("/data")
    public ResponseEntity<Void> receiveData(@RequestBody SensorDataDTO sensorDataDTO) {
        sensorDataProducer.sendSensorData(sensorDataDTO);
        return ResponseEntity.accepted().build();
    }
}
