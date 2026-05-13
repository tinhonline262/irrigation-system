package com.irrigation_system.iot.queue.consumer;

import com.irrigation_system.iot.config.RabbitMQConfig;
import com.irrigation_system.iot.dto.DeviceRegisterDTO;
import com.irrigation_system.iot.dto.DeviceStatusDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceMqttConsumer {

    private final DeviceRepository deviceRepository;

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.DEVICE_REGISTER_QUEUE, containerFactory = "singleRabbitListenerContainerFactory")
    public void handleDeviceRegister(DeviceRegisterDTO request) {
        log.info("Received device register request for chipId: {}", request.getChipId());
        if (request.getChipId() == null || request.getChipId().trim().isEmpty()) {
            log.warn("chipId is missing in registration request");
            return;
        }

        Optional<Device> existingDevice = deviceRepository.findByChipId(request.getChipId());
        if (existingDevice.isEmpty()) {
            Device newDevice = new Device();
            newDevice.setChipId(request.getChipId());
            newDevice.setName("ESP Device " + request.getChipId());
            newDevice.setStatus("online");
            newDevice.setLastSeenAt(Instant.now());
            newDevice.setAutoWaterEnabled(false);
            newDevice.setMoistureThresholdLow(30.0f);
            newDevice.setMoistureThresholdHigh(80.0f);
            deviceRepository.save(newDevice);
            log.info("Registered new unclaimed device with chipId: {}", request.getChipId());
        } else {
            Device device = existingDevice.get();
            device.setStatus("online");
            device.setLastSeenAt(Instant.now());
            deviceRepository.save(device);
            log.info("Updated existing device chipId: {} to online", request.getChipId());
        }
    }

    @Transactional
    @RabbitListener(queues = RabbitMQConfig.DEVICE_STATUS_QUEUE, containerFactory = "singleRabbitListenerContainerFactory")
    public void handleDeviceStatus(DeviceStatusDTO message) {
        log.info("Received status update {} for chipId: {}", message.getStatus(), message.getChipId());
        if (message.getChipId() != null) {
            deviceRepository.findByChipId(message.getChipId()).ifPresent(device -> {
                if (message.getStatus() != null) device.setStatus(message.getStatus());
                if (message.getRelay() != null) device.setStatusRelay(message.getRelay());
                if (message.getWifiRssi() != null) device.setWifiRssi(message.getWifiRssi());
                if (message.getIp() != null) device.setIp(message.getIp());
                if (message.getFreeHeap() != null) device.setFreeHeap(message.getFreeHeap());
                if (message.getUptime() != null) device.setUptime(message.getUptime());
                device.setLastSeenAt(Instant.now());
                deviceRepository.save(device);
            });
        }
    }
}