package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.CreateWateringScheduleDTO;
import com.irrigation_system.iot.dto.ToggleScheduleDTO;
import com.irrigation_system.iot.dto.UpdateWateringScheduleDTO;
import com.irrigation_system.iot.dto.WateringScheduleDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.entity.WateringSchedule;
import com.irrigation_system.iot.exception.ResourceNotFoundException;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.repository.WateringScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WateringScheduleServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private WateringScheduleRepository wateringScheduleRepository;

    @InjectMocks
    private WateringScheduleServiceImpl scheduleService;

    @Test
    void createSchedule_withValidCron_shouldCreateEnabledScheduleWithNextRunAt() {
        Device device = new Device();
        device.setId("device-1");
        UserEntity user = new UserEntity();
        user.setId("user-1");
        device.setUser(user);

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(wateringScheduleRepository.save(any(WateringSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateWateringScheduleDTO dto = new CreateWateringScheduleDTO();
        dto.setCronExpression("0 6 * * *");
        dto.setEnabled(true);

        WateringScheduleDTO result = scheduleService.createSchedule("device-1", "user-1", false, dto);

        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getNextRunAt()).isNotNull();
        assertThat(result.getCronExpression()).isEqualTo("0 6 * * *");
    }

    @Test
    void createSchedule_withInvalidCronValues_shouldThrowBadRequest() {
        Device device = new Device();
        device.setId("device-1");
        UserEntity user = new UserEntity();
        user.setId("user-1");
        device.setUser(user);

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));

        CreateWateringScheduleDTO dto = new CreateWateringScheduleDTO();
        dto.setCronExpression("99 99 * * *");

        assertThatThrownBy(() -> scheduleService.createSchedule("device-1", "user-1", false, dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Invalid cronExpression");
    }

    @Test
    void createSchedule_withFourFieldCron_shouldThrowBadRequest() {
        Device device = new Device();
        device.setId("device-1");
        UserEntity user = new UserEntity();
        user.setId("user-1");
        device.setUser(user);

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));

        CreateWateringScheduleDTO dto = new CreateWateringScheduleDTO();
        dto.setCronExpression("6 * * *");

        assertThatThrownBy(() -> scheduleService.createSchedule("device-1", "user-1", false, dto))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("cronExpression must have 5 fields");
    }

    @Test
    void createSchedule_whenEnabledFalse_shouldSetNextRunAtNull() {
        Device device = new Device();
        device.setId("device-1");
        UserEntity user = new UserEntity();
        user.setId("user-1");
        device.setUser(user);

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(wateringScheduleRepository.save(any(WateringSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateWateringScheduleDTO dto = new CreateWateringScheduleDTO();
        dto.setCronExpression("0 6 * * *");
        dto.setEnabled(false);

        WateringScheduleDTO result = scheduleService.createSchedule("device-1", "user-1", false, dto);

        assertThat(result.getEnabled()).isFalse();
        assertThat(result.getNextRunAt()).isNull();
    }

    @Test
    void toggleSchedule_whenDisabling_shouldSetNextRunAtNull() {
        Device device = new Device();
        device.setId("device-1");
        UserEntity user = new UserEntity();
        user.setId("user-1");
        device.setUser(user);

        WateringSchedule schedule = new WateringSchedule();
        schedule.setId("schedule-1");
        schedule.setDevice(device);
        schedule.setCronExpression("0 6 * * *");
        schedule.setEnabled(true);
        schedule.setNextRunAt(Instant.now().plusSeconds(3600));

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(wateringScheduleRepository.findByIdAndDevice_Id("schedule-1", "device-1")).thenReturn(Optional.of(schedule));
        when(wateringScheduleRepository.save(any(WateringSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ToggleScheduleDTO dto = new ToggleScheduleDTO();
        dto.setEnabled(false);

        WateringScheduleDTO result = scheduleService.toggleSchedule("device-1", "schedule-1", "user-1", false, dto);

        assertThat(result.getEnabled()).isFalse();
        assertThat(result.getNextRunAt()).isNull();
    }

    @Test
    void toggleSchedule_whenEnabling_shouldComputeNextRunAt() {
        Device device = new Device();
        device.setId("device-1");
        UserEntity user = new UserEntity();
        user.setId("user-1");
        device.setUser(user);

        WateringSchedule schedule = new WateringSchedule();
        schedule.setId("schedule-1");
        schedule.setDevice(device);
        schedule.setCronExpression("0 6 * * *");
        schedule.setEnabled(false);

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(wateringScheduleRepository.findByIdAndDevice_Id("schedule-1", "device-1")).thenReturn(Optional.of(schedule));
        when(wateringScheduleRepository.save(any(WateringSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ToggleScheduleDTO dto = new ToggleScheduleDTO();
        dto.setEnabled(true);

        WateringScheduleDTO result = scheduleService.toggleSchedule("device-1", "schedule-1", "user-1", false, dto);

        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getNextRunAt()).isNotNull();
    }

    @Test
    void updateSchedule_withNewCron_shouldRecalculateNextRunAt() {
        Device device = new Device();
        device.setId("device-1");
        UserEntity user = new UserEntity();
        user.setId("user-1");
        device.setUser(user);

        WateringSchedule schedule = new WateringSchedule();
        schedule.setId("schedule-1");
        schedule.setDevice(device);
        schedule.setCronExpression("0 6 * * *");
        schedule.setEnabled(true);

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(wateringScheduleRepository.findByIdAndDevice_Id("schedule-1", "device-1")).thenReturn(Optional.of(schedule));
        when(wateringScheduleRepository.save(any(WateringSchedule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateWateringScheduleDTO dto = new UpdateWateringScheduleDTO();
        dto.setCronExpression("0 7 * * *");

        WateringScheduleDTO result = scheduleService.updateSchedule("device-1", "schedule-1", "user-1", false, dto);

        assertThat(result.getCronExpression()).isEqualTo("0 7 * * *");
        assertThat(result.getNextRunAt()).isNotNull();
    }

    @Test
    void deleteSchedule_whenScheduleBelongsToDevice_shouldDelete() {
        Device device = new Device();
        device.setId("device-1");
        UserEntity user = new UserEntity();
        user.setId("user-1");
        device.setUser(user);

        WateringSchedule schedule = new WateringSchedule();
        schedule.setId("schedule-1");
        schedule.setDevice(device);

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(wateringScheduleRepository.findByIdAndDevice_Id("schedule-1", "device-1")).thenReturn(Optional.of(schedule));

        scheduleService.deleteSchedule("device-1", "schedule-1", "user-1", false);

        verify(wateringScheduleRepository).delete(schedule);
    }

    @Test
    void deleteSchedule_whenScheduleNotFound_throwsResourceNotFoundException() {
        Device device = new Device();
        device.setId("device-1");
        UserEntity user = new UserEntity();
        user.setId("user-1");
        device.setUser(user);

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(wateringScheduleRepository.findByIdAndDevice_Id("schedule-1", "device-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.deleteSchedule("device-1", "schedule-1", "user-1", false))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
