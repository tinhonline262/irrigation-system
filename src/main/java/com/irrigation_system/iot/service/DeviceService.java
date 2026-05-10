package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.CreateDeviceDTO;
import com.irrigation_system.iot.dto.DeviceDTO;

import com.irrigation_system.iot.dto.CalibrateDeviceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeviceService {
    DeviceDTO createDevice(CreateDeviceDTO createDeviceDTO);
    Page<DeviceDTO> getAllDevices(Pageable pageable);
    void deleteDevice(String id);
    DeviceDTO calibrateDevice(String id, CalibrateDeviceDTO calibrateDto);
}
