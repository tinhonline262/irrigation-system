package com.irrigation_system.iot.queue.consumer;

import com.irrigation_system.iot.config.RabbitMQConfig;
import com.irrigation_system.iot.dto.SensorDataDTO;
import com.irrigation_system.iot.entity.AirSensorReading;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.SoilSensorReading;
import com.irrigation_system.iot.dto.DashboardSummaryDTO;
import com.irrigation_system.iot.repository.AirSensorReadingRepository;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.repository.SoilSensorReadingRepository;
import com.irrigation_system.iot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorDataConsumer {

    private final DeviceRepository deviceRepository;
    private final AirSensorReadingRepository airSensorReadingRepository;
    private final SoilSensorReadingRepository soilSensorReadingRepository;
    private final DashboardService dashboardService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.SENSOR_DATA_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void receiveSensorDataBatch(List<SensorDataDTO> batch) {
        if (batch.isEmpty()) {
            return;
        }

        // Get all unique chipIds
        List<String> chipIds = batch.stream()
                .map(SensorDataDTO::getChipId)
                .distinct()
                .collect(Collectors.toList());

        // Fetch devices from DB by chipId
        Map<String, Device> deviceMap = deviceRepository.findByChipIdIn(chipIds).stream()
                .collect(Collectors.toMap(Device::getChipId, Function.identity()));

        List<AirSensorReading> airReadings = new ArrayList<>();
        List<SoilSensorReading> soilReadings = new ArrayList<>();
        Set<String> updatedDeviceIds = new LinkedHashSet<>();

        for (SensorDataDTO dto : batch) {
            Device device = deviceMap.get(dto.getChipId());
            if (device == null) {
                log.warn("Device with chipId {} not found. Skipping sensor reading.", dto.getChipId());
                continue;
            }

            Instant timestamp = dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now();

            if (dto.getTemperature() != null && dto.getHumidity() != null) {
                AirSensorReading airReading = new AirSensorReading();
                airReading.setId(UUID.randomUUID().toString());
                airReading.setDevice(device);
                airReading.setTemperatureCelsius(dto.getTemperature().floatValue());
                airReading.setHumidityPercent(dto.getHumidity().floatValue());
                airReading.setRecordedAt(timestamp);
                airReadings.add(airReading);
                updatedDeviceIds.add(device.getId());
            }

            if (dto.getSoilMoisture() != null) {
                SoilSensorReading soilReading = new SoilSensorReading();
                soilReading.setId(UUID.randomUUID().toString());
                soilReading.setDevice(device);
                soilReading.setMoisturePercent(dto.getSoilMoisture().floatValue());
                soilReading.setRecordedAt(timestamp);
                soilReadings.add(soilReading);
                updatedDeviceIds.add(device.getId());
            }

            if (dto.getRelay() != null) {
                device.setStatusRelay(dto.getRelay());
                updatedDeviceIds.add(device.getId());
            }
        }

        if (!airReadings.isEmpty()) {
            airSensorReadingRepository.saveAll(airReadings);
        }
        if (!soilReadings.isEmpty()) {
            soilSensorReadingRepository.saveAll(soilReadings);
        }

        for (String deviceId : updatedDeviceIds) {
            DashboardSummaryDTO summary = dashboardService.getDashboardSummaryInternal(deviceId);
            messagingTemplate.convertAndSend("/topic/dashboard/" + deviceId, summary);
        }
    }
}
