package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.DeviceConfigDTO;
import com.irrigation_system.iot.dto.UpdateDeviceConfigDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.exception.ResourceNotFoundException;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.service.AuditLogService;
import com.irrigation_system.iot.service.DeviceConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceConfigServiceImpl implements DeviceConfigService {

    private final DeviceRepository deviceRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public DeviceConfigDTO getDeviceConfig(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        log.info("Fetching config for device {}", deviceId);
        return mapToConfigDTO(device);
    }

    @Override
    @Transactional
    public DeviceConfigDTO updateDeviceConfig(String deviceId, UpdateDeviceConfigDTO dto) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        if (dto.getMoistureThresholdLow() != null) {
            device.setMoistureThresholdLow(dto.getMoistureThresholdLow());
        }
        if (dto.getMoistureThresholdHigh() != null) {
            device.setMoistureThresholdHigh(dto.getMoistureThresholdHigh());
        }
        if (dto.getAutoWaterEnabled() != null) {
            device.setAutoWaterEnabled(dto.getAutoWaterEnabled());
        }

        device = deviceRepository.save(device);
        log.info("Updated config for device {}", deviceId);

        auditLogService.logAction(
                "UPDATE_DEVICE_CONFIG",
                device.getId(),
                "{\"autoWaterEnabled\":" + device.getAutoWaterEnabled()
                        + ",\"moistureThresholdLow\":" + device.getMoistureThresholdLow()
                        + ",\"moistureThresholdHigh\":" + device.getMoistureThresholdHigh() + "}"
        );

        return mapToConfigDTO(device);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private DeviceConfigDTO mapToConfigDTO(Device device) {
        DeviceConfigDTO dto = new DeviceConfigDTO();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setStatus(device.getStatus());
        dto.setMoistureThresholdLow(device.getMoistureThresholdLow());
        dto.setMoistureThresholdHigh(device.getMoistureThresholdHigh());
        dto.setAutoWaterEnabled(device.getAutoWaterEnabled());
        dto.setLastSeenAt(device.getLastSeenAt());
        dto.setSoilMoistureOffset(device.getSoilMoistureOffset());
        dto.setAirTemperatureOffset(device.getAirTemperatureOffset());
        dto.setAirHumidityOffset(device.getAirHumidityOffset());
        return dto;
    }
}