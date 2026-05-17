package com.irrigation_system.iot.service;

import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.WateringLog;
import com.irrigation_system.iot.entity.WateringSchedule;
import com.irrigation_system.iot.properties.IrrigationScheduleProperties;
import com.irrigation_system.iot.queue.producer.DeviceControlProducer;
import com.irrigation_system.iot.repository.WateringLogRepository;
import com.irrigation_system.iot.repository.WateringScheduleRepository;
import com.irrigation_system.iot.utility.WateringCronUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Per-schedule transactional processor (separate bean so {@link Transactional} applies per call).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledWateringScheduleProcessor {

    private static final String TRIGGER_TYPE_SCHEDULE = "schedule";

    private final WateringScheduleRepository wateringScheduleRepository;
    private final WateringLogRepository wateringLogRepository;
    private final DeviceControlProducer deviceControlProducer;
    private final IrrigationScheduleProperties irrigationProperties;
    private final WateringNotificationService wateringNotificationService;

    @Transactional
    public Optional<ScheduledWateringRun> processOneSchedule(String scheduleId) {
        WateringSchedule schedule = wateringScheduleRepository.findByIdWithLock(scheduleId).orElse(null);
        if (schedule == null) {
            return Optional.empty();
        }

        Instant now = Instant.now();
        if (!Boolean.TRUE.equals(schedule.getEnabled())
                || schedule.getNextRunAt() == null
                || schedule.getNextRunAt().isAfter(now)) {
            return Optional.empty();
        }

        Device device = schedule.getDevice();
        if (device == null) {
            log.warn("Schedule {} has no device; advancing nextRunAt (skip-missed)", scheduleId);
            advanceNextRunAt(schedule);
            return Optional.empty();
        }

        // If the schedule's nextRunAt is more than 5 minutes in the past, it's a missed schedule
        // (e.g., system was offline). We clean/skip it by advancing nextRunAt without watering.
        if (now.isAfter(schedule.getNextRunAt().plusSeconds(300))) {
            log.warn("Schedule {} is missed (nextRunAt={}, now={}). Skipping watering and advancing nextRunAt.", scheduleId, schedule.getNextRunAt(), now);
            advanceNextRunAt(schedule);
            return Optional.empty();
        }

        String chipId = device.getChipId();
        if (chipId == null || chipId.isBlank()) {
            log.warn("Schedule {} device {} has no chipId; advancing nextRunAt (skip-missed)", scheduleId, device.getId());
            advanceNextRunAt(schedule);
            return Optional.empty();
        }

        if (wateringLogRepository.findFirstByDevice_IdAndEndedAtIsNullOrderByStartedAtDesc(device.getId()).isPresent()) {
            log.warn("Schedule {} skipped: device {} already watering; advancing nextRunAt (skip-missed)", scheduleId, device.getId());
            advanceNextRunAt(schedule);
            return Optional.empty();
        }

        WateringLog wateringLog = new WateringLog();
        wateringLog.setId(UUID.randomUUID().toString());
        wateringLog.setDevice(device);
        wateringLog.setTriggeredBy(null);
        wateringLog.setTriggerType(TRIGGER_TYPE_SCHEDULE);
        wateringLog.setStartedAt(now);
        wateringLogRepository.save(wateringLog);

        deviceControlProducer.sendControlCommand(chipId, "ON");

        // skip-missed: next slot from now, not from missed nextRunAt
        advanceNextRunAt(schedule);

        var duration = irrigationProperties.computeWateringDuration(schedule.getWaterAmountMl());
        log.info("Started scheduled watering log {} for device {} schedule {}", wateringLog.getId(), device.getId(), scheduleId);
        wateringNotificationService.notifyScheduleTriggered(device, schedule.getWaterAmountMl());

        return Optional.of(new ScheduledWateringRun(
                wateringLog.getId(),
                chipId,
                schedule.getWaterAmountMl(),
                duration
        ));
    }

    private void advanceNextRunAt(WateringSchedule schedule) {
        schedule.setNextRunAt(WateringCronUtils.computeNextRunAt(schedule.getCronExpression()));
        wateringScheduleRepository.save(schedule);
    }
}
