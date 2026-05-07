package com.irrigation_system.iot.service;

import com.irrigation_system.iot.config.RabbitMQConfig;
import com.irrigation_system.iot.dto.SensorDataDTO;
import com.irrigation_system.iot.entity.SensorDataEntity;
import com.irrigation_system.iot.repository.SensorDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorDataConsumer {

    private final SensorDataRepository sensorDataRepository;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.SENSOR_DATA_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void receiveSensorDataBatch(List<SensorDataDTO> batch) {
        log.info("Received batch of {} sensor data items", batch.size());
        if (batch.isEmpty()) {
            return;
        }

        List<SensorDataEntity> entities = batch.stream().map(dto -> {
            SensorDataEntity entity = new SensorDataEntity();
            entity.setDeviceId(dto.getDeviceId());
            entity.setTemperature(dto.getTemperature());
            entity.setHumidity(dto.getHumidity());
            entity.setSoilMoisture(dto.getSoilMoisture());
            entity.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp() : java.time.LocalDateTime.now());
            return entity;
        }).collect(Collectors.toList());

        sensorDataRepository.saveAll(entities);
        log.info("Successfully saved batch of {} sensor data items", entities.size());
    }
}
