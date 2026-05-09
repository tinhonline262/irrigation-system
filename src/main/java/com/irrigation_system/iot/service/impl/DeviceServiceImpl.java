package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.CreateDeviceDTO;
import com.irrigation_system.iot.dto.DeviceDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.exception.ResourceNotFoundException;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.repository.UserRepository;
import com.irrigation_system.iot.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

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

        DeviceDTO deviceDTO = new DeviceDTO();
        deviceDTO.setId(device.getId());
        deviceDTO.setName(device.getName());
        deviceDTO.setUserId(user.getId());
        deviceDTO.setUsername(user.getUsername());
        deviceDTO.setStatus(device.getStatus());
        deviceDTO.setAutoWaterEnabled(device.getAutoWaterEnabled());

        return deviceDTO;
    }
}
