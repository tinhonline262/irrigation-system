package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.CalibrateDeviceDTO;
import com.irrigation_system.iot.dto.CreateDeviceDTO;
import com.irrigation_system.iot.dto.DeviceDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.exception.ResourceNotFoundException;
import com.irrigation_system.iot.mapper.DeviceMapper;
import com.irrigation_system.iot.queue.producer.DeviceControlProducer;
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

import com.irrigation_system.iot.utility.AuthenticationUtils;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final DeviceMapper deviceMapper;
    private final AuditLogService auditLogService;
    private final DeviceControlProducer deviceControlProducer;

    @Override
    @Transactional
    public DeviceDTO createDevice(CreateDeviceDTO createDeviceDTO) {
        UserEntity user = userRepository.findById(createDeviceDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", createDeviceDTO.getUserId()));

        Device device = new Device();
        device.setUser(user);
        device.setName(createDeviceDTO.getName());
        device.setStatus("offline");
        device.setStatusRelay(false);
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
        auditLogService.logAction("CREATE_DEVICE", device.getId(), java.util.Map.of("deviceName", device.getName()));
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


    private UserEntity getCurrentUserEntity() {
        String username = AuthenticationUtils.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceDTO> getMyDevices() {
        UserEntity currentUser = getCurrentUserEntity();
        return deviceRepository.findAll().stream()
                .filter(d -> d.getUser() != null && d.getUser().getId().equals(currentUser.getId()))
                .map(deviceMapper::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DeviceDTO claimDevice(String chipId) {
        UserEntity currentUser = getCurrentUserEntity();
        Device device = deviceRepository.findByChipId(chipId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "chipId", chipId));

        if (device.getUser() != null) {
            if (device.getUser().getId().equals(currentUser.getId())) {
                return deviceMapper.mapToDTO(device); // Already owned by this user
            }
            throw new IllegalStateException("Device is already claimed by another user");
        }

        device.setUser(currentUser);
        device.setOwnerId(currentUser.getId());
        device.setClaimedAt(java.time.Instant.now());
        device = deviceRepository.save(device);

        auditLogService.logAction("CLAIM_DEVICE", device.getId(), java.util.Map.of("chipId", chipId));

        return deviceMapper.mapToDTO(device);
    }

    @Override
    @Transactional
    public void unclaimDevice(String deviceId) {
        UserEntity currentUser = getCurrentUserEntity();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        if (device.getUser() == null || !device.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You do not own this device");
        }

        device.setUser(null);
        device.setClaimedAt(null);
        deviceRepository.save(device);

        auditLogService.logAction("UNCLAIM_DEVICE", device.getId(), java.util.Map.of());
    }

    @Override
    @Transactional
    public DeviceDTO updateDeviceName(String deviceId, String newName) {
        UserEntity currentUser = getCurrentUserEntity();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        if (device.getUser() == null || !device.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You do not own this device");
        }

        device.setName(newName);
        device = deviceRepository.save(device);
        return deviceMapper.mapToDTO(device);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceDTO getMyDeviceDetail(String deviceId) {
        UserEntity currentUser = getCurrentUserEntity();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        if (device.getUser() == null || !device.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You do not own this device");
        }

        return deviceMapper.mapToDTO(device);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceDTO getAdminDeviceDetail(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));
        return deviceMapper.mapToDTO(device);
    }
}