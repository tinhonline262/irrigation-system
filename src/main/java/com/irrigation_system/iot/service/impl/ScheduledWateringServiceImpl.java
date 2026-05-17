package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.entity.WateringLog;
import com.irrigation_system.iot.entity.WateringSchedule;
import com.irrigation_system.iot.queue.producer.DeviceControlProducer;
import com.irrigation_system.iot.repository.WateringLogRepository;
import com.irrigation_system.iot.repository.WateringScheduleRepository;
import com.irrigation_system.iot.service.ScheduledWateringRun;
import com.irrigation_system.iot.service.ScheduledWateringScheduleProcessor;
import com.irrigation_system.iot.service.ScheduledWateringService;
import com.irrigation_system.iot.service.WateringNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Orchestrates scheduled watering: poll due schedules, start runs, schedule in-memory OFF.
 * <p>
 * Not safe for horizontal scaling without distributed lock (e.g. ShedLock).
 * Running multiple containers may execute the same schedule twice.
 * <p>
 * v1 relies on in-memory {@link TaskScheduler} for OFF; see Phase 2 (persist {@code scheduled_stop_at})
 * for restart-safe stop.
 */
@Service
@Slf4j
public class ScheduledWateringServiceImpl implements ScheduledWateringService {

    private final WateringScheduleRepository wateringScheduleRepository;
    private final WateringLogRepository wateringLogRepository;
    private final ScheduledWateringScheduleProcessor scheduleProcessor;
    private final DeviceControlProducer deviceControlProducer;
    private final TaskScheduler wateringScheduleTaskScheduler;
    private final WateringNotificationService wateringNotificationService;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private ScheduledWateringService self;

    public ScheduledWateringServiceImpl(
            WateringScheduleRepository wateringScheduleRepository,
            WateringLogRepository wateringLogRepository,
            ScheduledWateringScheduleProcessor scheduleProcessor,
            DeviceControlProducer deviceControlProducer,
            @Qualifier("wateringScheduleTaskScheduler") TaskScheduler wateringScheduleTaskScheduler,
            WateringNotificationService wateringNotificationService) {
        this.wateringScheduleRepository = wateringScheduleRepository;
        this.wateringLogRepository = wateringLogRepository;
        this.scheduleProcessor = scheduleProcessor;
        this.deviceControlProducer = deviceControlProducer;
        this.wateringScheduleTaskScheduler = wateringScheduleTaskScheduler;
        this.wateringNotificationService = wateringNotificationService;
    }

    @Override
    public void processDueSchedules() {
        Instant now = Instant.now();
        var dueSchedules = wateringScheduleRepository.findByEnabledTrueAndNextRunAtLessThanEqual(now);
        if (dueSchedules.isEmpty()) {
            return;
        }
        log.info("Processing {} due watering schedule(s) - now={}", dueSchedules.size(), java.time.ZonedDateTime.now());
        for (WateringSchedule schedule : dueSchedules) {
            log.info("  Schedule {} nextRunAt={}", schedule.getId(), schedule.getNextRunAt());
            try {
                scheduleProcessor.processOneSchedule(schedule.getId())
                        .ifPresent(this::scheduleOffTask);
            } catch (Exception ex) {
                log.error("Failed to process watering schedule {}", schedule.getId(), ex);
            }
        }
    }

    private void scheduleOffTask(ScheduledWateringRun run) {
        Instant stopAt = Instant.now().plus(run.duration());
        wateringScheduleTaskScheduler.schedule(
                () -> self.completeScheduledWatering(run.wateringLogId(), run.chipId(), run.waterAmountMl()),
                stopAt
        );
    }

    @Override
    @Transactional
    public void completeScheduledWatering(String wateringLogId, String chipId, Float waterAmountMl) {
        deviceControlProducer.sendControlCommand(chipId, "OFF");

        WateringLog wateringLog = wateringLogRepository.findById(wateringLogId).orElse(null);
        if (wateringLog == null) {
            log.warn("Scheduled watering log {} not found for OFF completion", wateringLogId);
            return;
        }
        if (wateringLog.getEndedAt() != null) {
            return;
        }
        wateringLog.setEndedAt(Instant.now());
        wateringLog.setWaterAmountMl(waterAmountMl);
        wateringLogRepository.save(wateringLog);
        log.info("Completed scheduled watering log {}", wateringLogId);

        if (wateringLog.getDevice() != null) {
            wateringNotificationService.notifyWateringCompleted(wateringLog.getDevice(), waterAmountMl, true);
        }
    }
}
