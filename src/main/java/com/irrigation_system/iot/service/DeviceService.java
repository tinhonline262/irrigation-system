package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.CreateDeviceDTO;
import com.irrigation_system.iot.dto.DeviceDTO;

import com.irrigation_system.iot.dto.CalibrateDeviceDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeviceService {
    DeviceDTO createDevice(CreateDeviceDTO createDeviceDTO);
    Page<DeviceDTO> getAllDevices(Pageable pageable);
    void deleteDevice(String id);
    DeviceDTO calibrateDevice(String id, CalibrateDeviceDTO calibrateDto);
    List<DeviceDTO> getMyDevices();
    DeviceDTO claimDevice(String chipId);
    void unclaimDevice(String deviceId);
    DeviceDTO updateDeviceName(String deviceId, String newName);
    DeviceDTO getMyDeviceDetail(String deviceId);
    DeviceDTO getAdminDeviceDetail(String deviceId);
}
