package com.irrigation_system.iot.service;

import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.WateringLog;
import com.irrigation_system.iot.entity.WateringSchedule;
import com.irrigation_system.iot.properties.IrrigationScheduleProperties;
import com.irrigation_system.iot.queue.producer.DeviceControlProducer;
import com.irrigation_system.iot.service.WateringNotificationService;
import com.irrigation_system.iot.repository.WateringLogRepository;
import com.irrigation_system.iot.repository.WateringScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduledWateringScheduleProcessorTest {

    @Mock
    private WateringScheduleRepository wateringScheduleRepository;
    @Mock
    private WateringLogRepository wateringLogRepository;
    @Mock
    private DeviceControlProducer deviceControlProducer;
    @Mock
    private IrrigationScheduleProperties irrigationProperties;
    @Mock
    private WateringNotificationService wateringNotificationService;

    @InjectMocks
    private ScheduledWateringScheduleProcessor processor;

    private WateringSchedule schedule;
    private Device device;

    @BeforeEach
    void setUp() {
        device = new Device();
        device.setId("device-1");
        device.setChipId("chip-abc");

        schedule = new WateringSchedule();
        schedule.setId("sched-1");
        schedule.setEnabled(true);
        schedule.setNextRunAt(Instant.now().minusSeconds(60));
        schedule.setCronExpression("0 6 * * *");
        schedule.setWaterAmountMl(100f);
        schedule.setDevice(device);

        when(wateringScheduleRepository.findByIdWithLock("sched-1")).thenReturn(Optional.of(schedule));
    }

    @Test
    void processOneSchedule_whenNotBusy_startsWateringAndAdvancesNextRunAt() {
        when(irrigationProperties.computeWateringDuration(100f)).thenReturn(java.time.Duration.ofMinutes(1));
        when(wateringLogRepository.findFirstByDevice_IdAndEndedAtIsNullOrderByStartedAtDesc("device-1"))
                .thenReturn(Optional.empty());

        Optional<ScheduledWateringRun> result = processor.processOneSchedule("sched-1");

        assertThat(result).isPresent();
        verify(deviceControlProducer).sendControlCommand("chip-abc", "ON");
        verify(wateringNotificationService).notifyScheduleTriggered(device, 100f);
        verify(wateringLogRepository).save(any(WateringLog.class));
        verify(wateringScheduleRepository).save(schedule);
        assertThat(schedule.getNextRunAt()).isNotNull();
    }

    @Test
    void processOneSchedule_whenDeviceBusy_skipsOnButAdvancesNextRunAt() {
        WateringLog activeLog = new WateringLog();
        when(wateringLogRepository.findFirstByDevice_IdAndEndedAtIsNullOrderByStartedAtDesc("device-1"))
                .thenReturn(Optional.of(activeLog));

        Optional<ScheduledWateringRun> result = processor.processOneSchedule("sched-1");

        assertThat(result).isEmpty();
        verify(deviceControlProducer, never()).sendControlCommand(any(), any());
        verify(wateringScheduleRepository).save(schedule);
        assertThat(schedule.getNextRunAt()).isNotNull();
    }

    @Test
    void processOneSchedule_whenNoChipId_skipsOnButAdvancesNextRunAt() {
        device.setChipId(null);

        Optional<ScheduledWateringRun> result = processor.processOneSchedule("sched-1");

        assertThat(result).isEmpty();
        verify(deviceControlProducer, never()).sendControlCommand(any(), any());
        verify(wateringScheduleRepository).save(schedule);
    }

    @Test
    void processOneSchedule_persistsScheduleTriggerType() {
        when(irrigationProperties.computeWateringDuration(100f)).thenReturn(java.time.Duration.ofMinutes(1));
        when(wateringLogRepository.findFirstByDevice_IdAndEndedAtIsNullOrderByStartedAtDesc("device-1"))
                .thenReturn(Optional.empty());

        processor.processOneSchedule("sched-1");

        ArgumentCaptor<WateringLog> captor = ArgumentCaptor.forClass(WateringLog.class);
        verify(wateringLogRepository).save(captor.capture());
        assertThat(captor.getValue().getTriggerType()).isEqualTo("schedule");
        assertThat(captor.getValue().getTriggeredBy()).isNull();
    }
}
