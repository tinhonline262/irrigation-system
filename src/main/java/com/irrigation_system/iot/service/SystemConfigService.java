package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.SystemConfigDto;

import java.util.List;

public interface SystemConfigService {
    List<SystemConfigDto> getAllConfigs();
    SystemConfigDto updateConfig(String key, String value);
    String getConfigValue(String key, String defaultValue);
    byte[] exportBackup();
}
