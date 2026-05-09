package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.CreateDeviceDTO;
import com.irrigation_system.iot.dto.DeviceDTO;

public interface DeviceService {
    DeviceDTO createDevice(CreateDeviceDTO createDeviceDTO);
}
