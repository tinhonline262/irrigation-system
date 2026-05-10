package com.irrigation_system.iot.queue;

import com.irrigation_system.iot.config.RabbitMQConfig;
import com.irrigation_system.iot.dto.SensorDataDTO;
import com.irrigation_system.iot.entity.AirSensorReading;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.SoilSensorReading;
import com.irrigation_system.iot.repository.AirSensorReadingRepository;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.repository.SoilSensorReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.SENSOR_DATA_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void receiveSensorDataBatch(List<SensorDataDTO> batch) {
        log.info("Received batch of {} sensor data items", batch.size());
        if (batch.isEmpty()) {
            return;
        }

        // Get all unique deviceIds
        List<String> deviceIds = batch.stream()
                .map(SensorDataDTO::getDeviceId)
                .distinct()
                .collect(Collectors.toList());

        // Fetch devices from DB
        Map<String, Device> deviceMap = deviceRepository.findAllById(deviceIds).stream()
                .collect(Collectors.toMap(Device::getId, Function.identity()));

        List<AirSensorReading> airReadings = new ArrayList<>();
        List<SoilSensorReading> soilReadings = new ArrayList<>();

        for (SensorDataDTO dto : batch) {
            Device device = deviceMap.get(dto.getDeviceId());
            if (device == null) {
                log.warn("Device with id {} not found. Skipping sensor reading.", dto.getDeviceId());
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
            }

            if (dto.getSoilMoisture() != null) {
                SoilSensorReading soilReading = new SoilSensorReading();
                soilReading.setId(UUID.randomUUID().toString());
                soilReading.setDevice(device);
                soilReading.setMoisturePercent(dto.getSoilMoisture().floatValue());
                soilReading.setRecordedAt(timestamp);
                soilReadings.add(soilReading);
            }
        }

        if (!airReadings.isEmpty()) {
            airSensorReadingRepository.saveAll(airReadings);
        }
        if (!soilReadings.isEmpty()) {
            soilSensorReadingRepository.saveAll(soilReadings);
        }

        log.info("Successfully saved {} air readings and {} soil readings", airReadings.size(), soilReadings.size());
    }
}
