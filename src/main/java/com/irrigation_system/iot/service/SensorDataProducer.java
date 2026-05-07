package com.irrigation_system.iot.service;

import com.irrigation_system.iot.config.RabbitMQConfig;
import com.irrigation_system.iot.dto.SensorDataDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorDataProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendSensorData(SensorDataDTO data) {
        log.debug("Sending sensor data to queue: {}", data);
        rabbitTemplate.convertAndSend(RabbitMQConfig.SENSOR_DATA_QUEUE, data);
    }
}
