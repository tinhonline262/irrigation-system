package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.WateringLogDTO;
import com.irrigation_system.iot.dto.WateringLogStatsDTO;
import com.irrigation_system.iot.dto.WateringStatusDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.entity.WateringLog;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.repository.UserRepository;
import com.irrigation_system.iot.repository.WateringLogRepository;
import com.irrigation_system.iot.service.WateringNotificationService;
import com.irrigation_system.iot.utility.AuthenticationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WateringServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WateringLogRepository wateringLogRepository;

    @Mock
    private WateringNotificationService wateringNotificationService;

    @InjectMocks
    private WateringServiceImpl wateringService;

    @Test
    void startManualWatering_whenDeviceOwned_shouldCreateManualWateringLog() {
        Device device = new Device();
        device.setId("device-1");
        device.setOwnerId("user-1");

        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setUsername("tester");

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(wateringLogRepository.save(any(WateringLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<AuthenticationUtils> utilities = mockStatic(AuthenticationUtils.class)) {
            utilities.when(AuthenticationUtils::getCurrentUsername).thenReturn("tester");

            WateringLogDTO result = wateringService.startManualWatering("device-1");

            assertThat(result).isNotNull();
            assertThat(result.getDeviceId()).isEqualTo("device-1");
            assertThat(result.getTriggeredBy()).isEqualTo("tester");
            assertThat(result.getTriggerType()).isEqualTo("manual");
            assertThat(result.getStartedAt()).isNotNull();
            assertThat(result.getEndedAt()).isNull();
            assertThat(result.getWaterAmountMl()).isNull();

            verify(wateringLogRepository, times(1)).save(any(WateringLog.class));
        }
    }

    @Test
    void startManualWatering_whenDeviceNotFound_shouldThrowNotFound() {
        when(deviceRepository.findById("device-1")).thenReturn(Optional.empty());

        try (MockedStatic<AuthenticationUtils> utilities = mockStatic(AuthenticationUtils.class)) {
            utilities.when(AuthenticationUtils::getCurrentUsername).thenReturn("tester");

            assertThatThrownBy(() -> wateringService.startManualWatering("device-1"))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting("status")
                    .isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        }
    }

    @Test
    void startManualWatering_whenOwnerDoesNotMatch_shouldThrowForbidden() {
        Device device = new Device();
        device.setId("device-1");
        device.setOwnerId("owner-2");

        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setUsername("tester");

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));

        try (MockedStatic<AuthenticationUtils> utilities = mockStatic(AuthenticationUtils.class)) {
            utilities.when(AuthenticationUtils::getCurrentUsername).thenReturn("tester");

            assertThatThrownBy(() -> wateringService.startManualWatering("device-1"))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting("status")
                    .isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);
        }
    }

    @Test
    void startManualWatering_whenOwnerIdNull_shouldThrowForbidden() {
        Device device = new Device();
        device.setId("device-1");
        device.setOwnerId(null);

        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setUsername("tester");

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));

        try (MockedStatic<AuthenticationUtils> utilities = mockStatic(AuthenticationUtils.class)) {
            utilities.when(AuthenticationUtils::getCurrentUsername).thenReturn("tester");

            assertThatThrownBy(() -> wateringService.startManualWatering("device-1"))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting("status")
                    .isEqualTo(org.springframework.http.HttpStatus.FORBIDDEN);
        }
    }

    @Test
    void stopManualWatering_whenActiveSession_shouldSetEndedAtAndWaterAmount() {
        Device device = new Device();
        device.setId("device-1");
        device.setOwnerId("user-1");

        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setUsername("tester");

        WateringLog wateringLog = new WateringLog();
        wateringLog.setId("log-1");
        wateringLog.setDevice(device);
        wateringLog.setTriggerType("manual");
        wateringLog.setStartedAt(Instant.now().minusSeconds(30));

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(wateringLogRepository.findFirstByDevice_IdAndTriggerTypeAndEndedAtIsNullOrderByStartedAtDesc("device-1", "manual"))
                .thenReturn(Optional.of(wateringLog));
        when(wateringLogRepository.save(any(WateringLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        try (MockedStatic<AuthenticationUtils> utilities = mockStatic(AuthenticationUtils.class)) {
            utilities.when(AuthenticationUtils::getCurrentUsername).thenReturn("tester");

            WateringLogDTO result = wateringService.stopManualWatering("device-1");

            assertThat(result.getEndedAt()).isNotNull();
            assertThat(result.getWaterAmountMl()).isEqualTo(120f);
            assertThat(result.getTriggerType()).isEqualTo("manual");
            verify(wateringLogRepository, times(1)).save(any(WateringLog.class));
        }
    }

    @Test
    void stopManualWatering_whenNoActiveSession_shouldThrowBadRequest() {
        Device device = new Device();
        device.setId("device-1");
        device.setOwnerId("user-1");

        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setUsername("tester");

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(wateringLogRepository.findFirstByDevice_IdAndTriggerTypeAndEndedAtIsNullOrderByStartedAtDesc("device-1", "manual"))
                .thenReturn(Optional.empty());

        try (MockedStatic<AuthenticationUtils> utilities = mockStatic(AuthenticationUtils.class)) {
            utilities.when(AuthenticationUtils::getCurrentUsername).thenReturn("tester");

            assertThatThrownBy(() -> wateringService.stopManualWatering("device-1"))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting("status")
                    .isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
        }
    }

    @Test
    void getWateringStatus_whenActiveSession_shouldReturnRunningTrue() {
        Device device = new Device();
        device.setId("device-1");
        device.setOwnerId("user-1");

        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setUsername("tester");

        WateringLog wateringLog = new WateringLog();
        wateringLog.setId("log-1");
        wateringLog.setDevice(device);
        wateringLog.setStartedAt(Instant.now().minusSeconds(30));

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(wateringLogRepository.findFirstByDevice_IdAndEndedAtIsNullOrderByStartedAtDesc("device-1"))
                .thenReturn(Optional.of(wateringLog));

        try (MockedStatic<AuthenticationUtils> utilities = mockStatic(AuthenticationUtils.class)) {
            utilities.when(AuthenticationUtils::getCurrentUsername).thenReturn("tester");

            WateringStatusDTO status = wateringService.getWateringStatus("device-1");

            assertThat(status.isRunning()).isTrue();
            assertThat(status.getElapsedSeconds()).isGreaterThanOrEqualTo(29L);
            assertThat(status.getWateringLogId()).isEqualTo("log-1");
        }
    }

    @Test
    void getWateringStatus_whenNoActiveSession_shouldReturnRunningFalse() {
        Device device = new Device();
        device.setId("device-1");
        device.setOwnerId("user-1");

        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setUsername("tester");

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(wateringLogRepository.findFirstByDevice_IdAndEndedAtIsNullOrderByStartedAtDesc("device-1"))
                .thenReturn(Optional.empty());

        try (MockedStatic<AuthenticationUtils> utilities = mockStatic(AuthenticationUtils.class)) {
            utilities.when(AuthenticationUtils::getCurrentUsername).thenReturn("tester");

            WateringStatusDTO status = wateringService.getWateringStatus("device-1");

            assertThat(status.isRunning()).isFalse();
            assertThat(status.getWateringLogId()).isNull();
        }
    }

    @Test
    void exportWateringLogsCsv_shouldWriteBomAndHeader() throws Exception {
        Device device = new Device();
        device.setId("device-1");
        device.setOwnerId("user-1");

        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setUsername("tester");

        WateringLog wateringLog = new WateringLog();
        wateringLog.setId("log-1");
        wateringLog.setDevice(device);
        wateringLog.setTriggerType("manual");
        wateringLog.setStartedAt(Instant.parse("2026-05-15T08:00:00Z"));
        wateringLog.setEndedAt(Instant.parse("2026-05-15T08:05:00Z"));
        wateringLog.setWaterAmountMl(150f);

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        when(wateringLogRepository.findByDevice_IdOrderByStartedAtDesc("device-1"))
                .thenReturn(Stream.of(wateringLog));

        try (MockedStatic<AuthenticationUtils> utilities = mockStatic(AuthenticationUtils.class)) {
            utilities.when(AuthenticationUtils::getCurrentUsername).thenReturn("tester");

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            wateringService.exportWateringLogsCsv("device-1", output);
            String csv = output.toString(StandardCharsets.UTF_8);

            assertThat(csv).startsWith("\uFEFFid,deviceId,triggeredBy,triggerType,startedAt,endedAt,waterAmountMl");
            assertThat(csv).contains("log-1,device-1,,manual,2026-05-15T08:00:00Z,2026-05-15T08:05:00Z,150.0");
        }
    }

    @Test
    void getWateringLogStats_whenDeviceIdValid_shouldReturnStatsList() {
        Device device = new Device();
        device.setId("device-1");
        device.setOwnerId("user-1");

        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setUsername("tester");

        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));
        Object[] row = new Object[]{"2026-05-15", 1200f, 3L};
        List<Object[]> statsData = java.util.Collections.singletonList(row);
        when(wateringLogRepository.findDailyWateringStatsByDeviceId("device-1"))
                .thenReturn(statsData);

        try (MockedStatic<AuthenticationUtils> utilities = mockStatic(AuthenticationUtils.class)) {
            utilities.when(AuthenticationUtils::getCurrentUsername).thenReturn("tester");

            List<WateringLogStatsDTO> stats = wateringService.getWateringLogStats("device-1");

            assertThat(stats).hasSize(1);
            assertThat(stats.get(0).getDate()).isEqualTo("2026-05-15");
            assertThat(stats.get(0).getTotalWaterAmountMl()).isEqualTo(1200f);
            assertThat(stats.get(0).getWateringCount()).isEqualTo(3L);
        }
    }
}
