package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.CreateWateringScheduleDTO;
import com.irrigation_system.iot.dto.ToggleScheduleDTO;
import com.irrigation_system.iot.dto.UpdateWateringScheduleDTO;
import com.irrigation_system.iot.dto.WateringScheduleDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.WateringSchedule;
import com.irrigation_system.iot.exception.ResourceNotFoundException;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.repository.WateringScheduleRepository;
import com.irrigation_system.iot.service.WateringScheduleService;
import com.irrigation_system.iot.properties.IrrigationScheduleProperties;
import com.irrigation_system.iot.utility.WateringCronUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
@Slf4j
public class WateringScheduleServiceImpl implements WateringScheduleService {

    private final DeviceRepository deviceRepository;
    private final WateringScheduleRepository wateringScheduleRepository;
    private final IrrigationScheduleProperties irrigationProperties;

    @Override
    @Transactional(readOnly = true)
    public List<WateringScheduleDTO> listSchedules(String deviceId, String requesterUserId, boolean isAdmin) {
        Device device = requireOwnedDeviceOrAdmin(deviceId, requesterUserId, isAdmin);
        return wateringScheduleRepository.findByDevice_IdOrderByNextRunAtAsc(device.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public WateringScheduleDTO createSchedule(String deviceId, String requesterUserId, boolean isAdmin, CreateWateringScheduleDTO dto) {
        Device device = requireOwnedDeviceOrAdmin(deviceId, requesterUserId, isAdmin);

        WateringSchedule schedule = new WateringSchedule();
        schedule.setId(UUID.randomUUID().toString());
        schedule.setOl(0L);
        schedule.setCreatedAt(Instant.now());
        schedule.setDevice(device);
        schedule.setCronExpression(normalizeCron(dto.getCronExpression()));
        
        float calculatedMl = 0f;
        if (dto.getDurationInMinutes() != null) {
            calculatedMl = (float) (dto.getDurationInMinutes() * irrigationProperties.getFlowRateMlPerMinute());
        }
        schedule.setWaterAmountMl(calculatedMl);

        boolean enabled = dto.getEnabled() == null || dto.getEnabled();
        schedule.setEnabled(enabled);
        schedule.setNextRunAt(enabled ? WateringCronUtils.computeNextRunAt(schedule.getCronExpression()) : null);

        schedule = wateringScheduleRepository.save(schedule);
        log.info("Created watering schedule {} for device {}", schedule.getId(), deviceId);
        return toDto(schedule);
    }

    @Override
    @Transactional
    public WateringScheduleDTO updateSchedule(String deviceId, String scheduleId, String requesterUserId, boolean isAdmin, UpdateWateringScheduleDTO dto) {
        requireOwnedDeviceOrAdmin(deviceId, requesterUserId, isAdmin);

        WateringSchedule schedule = wateringScheduleRepository.findByIdAndDevice_Id(scheduleId, deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("WateringSchedule", "id", scheduleId));

        if (dto.getCronExpression() != null && !dto.getCronExpression().isBlank()) {
            schedule.setCronExpression(normalizeCron(dto.getCronExpression()));
        }
        if (dto.getDurationInMinutes() != null) {
            float calculatedMl = (float) (dto.getDurationInMinutes() * irrigationProperties.getFlowRateMlPerMinute());
            schedule.setWaterAmountMl(calculatedMl);
        }
        if (dto.getEnabled() != null) {
            schedule.setEnabled(dto.getEnabled());
        }

        schedule.setLastModifiedAt(Instant.now());
        schedule.setNextRunAt(schedule.getEnabled() ? WateringCronUtils.computeNextRunAt(schedule.getCronExpression()) : null);

        schedule = wateringScheduleRepository.save(schedule);
        return toDto(schedule);
    }

    @Override
    @Transactional
    public WateringScheduleDTO toggleSchedule(String deviceId, String scheduleId, String requesterUserId, boolean isAdmin, ToggleScheduleDTO dto) {
        requireOwnedDeviceOrAdmin(deviceId, requesterUserId, isAdmin);

        WateringSchedule schedule = wateringScheduleRepository.findByIdAndDevice_Id(scheduleId, deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("WateringSchedule", "id", scheduleId));

        schedule.setEnabled(dto.getEnabled());
        schedule.setLastModifiedAt(Instant.now());
        schedule.setNextRunAt(schedule.getEnabled() ? WateringCronUtils.computeNextRunAt(schedule.getCronExpression()) : null);

        schedule = wateringScheduleRepository.save(schedule);
        return toDto(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(String deviceId, String scheduleId, String requesterUserId, boolean isAdmin) {
        requireOwnedDeviceOrAdmin(deviceId, requesterUserId, isAdmin);

        WateringSchedule schedule = wateringScheduleRepository.findByIdAndDevice_Id(scheduleId, deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("WateringSchedule", "id", scheduleId));

        wateringScheduleRepository.delete(schedule);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private Device requireOwnedDeviceOrAdmin(String deviceId, String requesterUserId, boolean isAdmin) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        if (isAdmin) {
            return device;
        }

        String ownerId = device.getUser() != null ? device.getUser().getId() : null;
        if (ownerId != null && ownerId.equals(requesterUserId)) {
            return device;
        }

        throw new ResponseStatusException(FORBIDDEN, "You don't have permission to access this device");
    }

    private String normalizeCron(String cronExpression) {
        String cron = cronExpression == null ? "" : cronExpression.trim().replaceAll("\\s+", " ");
        String[] parts = cron.split(" ");
        if (parts.length != 5) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "cronExpression must have 5 fields (m h dom mon dow)");
        }
        // Validate by parsing as Spring CronExpression (expects 6 fields, add seconds)
        try {
            CronExpression.parse("0 " + cron);
        } catch (Exception e) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid cronExpression: " + e.getMessage());
        }
        return cron;
    }

    private WateringScheduleDTO toDto(WateringSchedule schedule) {
        WateringScheduleDTO dto = new WateringScheduleDTO();
        dto.setId(schedule.getId());
        dto.setDeviceId(schedule.getDevice() != null ? schedule.getDevice().getId() : null);
        dto.setCronExpression(schedule.getCronExpression());
        
        if (schedule.getWaterAmountMl() != null) {
            long durationMins = Math.round(schedule.getWaterAmountMl() / irrigationProperties.getFlowRateMlPerMinute());
            dto.setDurationInMinutes(durationMins);
        }
        
        dto.setEnabled(schedule.getEnabled());
        dto.setNextRunAt(schedule.getNextRunAt());
        return dto;
    }
}

