package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.WateringLogDTO;
import com.irrigation_system.iot.dto.WateringLogPageDTO;
import com.irrigation_system.iot.dto.WateringLogStatsDTO;
import com.irrigation_system.iot.dto.WateringStatusDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.UserEntity;

import java.io.OutputStream;
import java.util.List;

public interface WateringService {

    class DeviceAndUser {
        public final Device device;
        public final UserEntity user;

        public DeviceAndUser(Device device, UserEntity user) {
            this.device = device;
            this.user = user;
        }
    }

    DeviceAndUser verifyDeviceOwnership(String deviceId);
    
    WateringLogDTO startManualWatering(String deviceId);
    
    WateringLogDTO stopManualWatering(String deviceId);
    
    WateringStatusDTO getWateringStatus(String deviceId);
    
    WateringLogPageDTO getWateringLogs(String deviceId, int page, int size);
    
    void exportWateringLogsCsv(String deviceId, OutputStream outputStream);
    
    List<WateringLogStatsDTO> getWateringLogStats(String deviceId);
}
