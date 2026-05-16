package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.entity.WateringLog;
import com.irrigation_system.iot.queue.producer.DeviceControlProducer;
import com.irrigation_system.iot.repository.WateringLogRepository;
import com.irrigation_system.iot.repository.WateringScheduleRepository;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.service.ScheduledWateringScheduleProcessor;
import com.irrigation_system.iot.service.WateringNotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledWateringServiceImplTest {

    @Mock
    private WateringScheduleRepository wateringScheduleRepository;
    @Mock
    private WateringLogRepository wateringLogRepository;
    @Mock
    private ScheduledWateringScheduleProcessor scheduleProcessor;
    @Mock
    private DeviceControlProducer deviceControlProducer;
    @Mock
    private TaskScheduler wateringScheduleTaskScheduler;
    @Mock
    private WateringNotificationService wateringNotificationService;

    @InjectMocks
    private ScheduledWateringServiceImpl scheduledWateringService;

    @Test
    void completeScheduledWatering_whenLogAlreadyClosed_sendsOffButDoesNotUpdateLog() {
        String logId = "log-1";
        String chipId = "chip-1";
        WateringLog wateringLog = new WateringLog();
        wateringLog.setId(logId);
        wateringLog.setEndedAt(Instant.now());
        when(wateringLogRepository.findById(logId)).thenReturn(Optional.of(wateringLog));

        scheduledWateringService.completeScheduledWatering(logId, chipId, 100f);

        verify(deviceControlProducer).sendControlCommand(chipId, "OFF");
        verify(wateringLogRepository, never()).save(any());
    }

    @Test
    void completeScheduledWatering_whenLogOpen_closesLogAndSendsOff() {
        String logId = "log-2";
        String chipId = "chip-2";
        Device device = new Device();
        device.setId("device-2");
        device.setName("Garden");
        device.setOwnerId("user-1");
        WateringLog wateringLog = new WateringLog();
        wateringLog.setId(logId);
        wateringLog.setDevice(device);
        when(wateringLogRepository.findById(logId)).thenReturn(Optional.of(wateringLog));

        scheduledWateringService.completeScheduledWatering(logId, chipId, 75f);

        verify(deviceControlProducer).sendControlCommand(chipId, "OFF");
        verify(wateringLogRepository).save(wateringLog);
        verify(wateringNotificationService).notifyWateringCompleted(device, 75f, true);
        assertNotNull(wateringLog.getEndedAt());
        assertEquals(75f, wateringLog.getWaterAmountMl());
    }

    @Test
    void completeScheduledWatering_whenLogMissing_stillSendsOff() {
        when(wateringLogRepository.findById("missing")).thenReturn(Optional.empty());

        scheduledWateringService.completeScheduledWatering("missing", "chip-x", 50f);

        verify(deviceControlProducer).sendControlCommand(eq("chip-x"), eq("OFF"));
        verify(wateringLogRepository, never()).save(any());
    }
}
