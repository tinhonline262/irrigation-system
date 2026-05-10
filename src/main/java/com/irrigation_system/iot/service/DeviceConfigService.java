package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.DeviceConfigDTO;
import com.irrigation_system.iot.dto.UpdateDeviceConfigDTO;

public interface DeviceConfigService {

    DeviceConfigDTO getDeviceConfig(String deviceId);

    DeviceConfigDTO updateDeviceConfig(String deviceId, UpdateDeviceConfigDTO updateDeviceConfigDTO);
}