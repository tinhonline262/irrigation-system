package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.CalibrateDeviceDTO;
import com.irrigation_system.iot.dto.CreateDeviceDTO;
import com.irrigation_system.iot.dto.DeviceDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.exception.ResourceNotFoundException;
import com.irrigation_system.iot.mapper.DeviceMapper;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.repository.UserRepository;
import com.irrigation_system.iot.service.AuditLogService;
import com.irrigation_system.iot.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final DeviceMapper deviceMapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public DeviceDTO createDevice(CreateDeviceDTO createDeviceDTO) {
        UserEntity user = userRepository.findById(createDeviceDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", createDeviceDTO.getUserId()));

        Device device = new Device();
        device.setUser(user);
        device.setName(createDeviceDTO.getName());
        device.setStatus("offline");
        device.setAutoWaterEnabled(false);
        device.setCreatedAt(Instant.now());

        device = deviceRepository.save(device);
        log.info("Device {} created", device.getName());

        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setId(device.getId());
        deviceDTO.setName(device.getName());
        deviceDTO.setUserId(user.getId());
        deviceDTO.setUsername(user.getUsername());
        deviceDTO.setStatus(device.getStatus());
        deviceDTO.setAutoWaterEnabled(device.getAutoWaterEnabled());
        auditLogService.logAction("CREATE_DEVICE", device.getId(), "{\"deviceName\":\"" + device.getName() + "\"}");
        return deviceDTO;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<DeviceDTO> getAllDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable)
                .map(deviceMapper::mapToDTO);
    }

    @Override
    @Transactional
    public void deleteDevice(String id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));
        deviceRepository.delete(device);
    }

    @Override
    @Transactional
    public DeviceDTO calibrateDevice(String id, CalibrateDeviceDTO calibrateDto) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        if (calibrateDto.getSoilMoistureOffset() != null) {
            device.setSoilMoistureOffset(calibrateDto.getSoilMoistureOffset());
        }
        if (calibrateDto.getAirTemperatureOffset() != null) {
            device.setAirTemperatureOffset(calibrateDto.getAirTemperatureOffset());
        }
        if (calibrateDto.getAirHumidityOffset() != null) {
            device.setAirHumidityOffset(calibrateDto.getAirHumidityOffset());
        }

        device = deviceRepository.save(device);
        return deviceMapper.mapToDTO(device);
    }
}
