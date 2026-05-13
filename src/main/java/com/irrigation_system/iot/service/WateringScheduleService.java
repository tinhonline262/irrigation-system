package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.CreateWateringScheduleDTO;
import com.irrigation_system.iot.dto.ToggleScheduleDTO;
import com.irrigation_system.iot.dto.UpdateWateringScheduleDTO;
import com.irrigation_system.iot.dto.WateringScheduleDTO;

import java.util.List;

public interface WateringScheduleService {

    List<WateringScheduleDTO> listSchedules(String deviceId, String requesterUserId, boolean isAdmin);

    WateringScheduleDTO createSchedule(String deviceId, String requesterUserId, boolean isAdmin, CreateWateringScheduleDTO dto);

    WateringScheduleDTO updateSchedule(String deviceId, String scheduleId, String requesterUserId, boolean isAdmin, UpdateWateringScheduleDTO dto);

    WateringScheduleDTO toggleSchedule(String deviceId, String scheduleId, String requesterUserId, boolean isAdmin, ToggleScheduleDTO dto);

    void deleteSchedule(String deviceId, String scheduleId, String requesterUserId, boolean isAdmin);
}

