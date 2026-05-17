package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.CalibrateDeviceDTO;
import com.irrigation_system.iot.dto.CreateDeviceDTO;
import com.irrigation_system.iot.dto.DeviceDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.exception.ResourceNotFoundException;
import com.irrigation_system.iot.mapper.DeviceMapper;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.repository.UserRepository;
import com.irrigation_system.iot.service.AuditLogService;
import com.irrigation_system.iot.queue.producer.DeviceControlProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DeviceMapper deviceMapper;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private DeviceControlProducer deviceControlProducer;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    @Test
    void calibrateDevice_withSoilMoistureOffset_updatesOffset() {
        Device device = new Device();
        device.setId("device-1");
        device.setSoilMoistureOffset(0f);
        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deviceMapper.mapToDTO(any(Device.class))).thenReturn(new DeviceDTO());

        CalibrateDeviceDTO dto = new CalibrateDeviceDTO();
        dto.setSoilMoistureOffset(2.5f);

        deviceService.calibrateDevice("device-1", dto);

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        verify(deviceRepository).save(captor.capture());
        assertThat(captor.getValue().getSoilMoistureOffset()).isEqualTo(2.5f);
    }

    @Test
    void calibrateDevice_withAirTemperatureOffset_onlyUpdatesThatField() {
        Device device = new Device();
        device.setId("device-1");
        device.setSoilMoistureOffset(1.0f);
        device.setAirTemperatureOffset(0f);
        when(deviceRepository.findById("device-1")).thenReturn(Optional.of(device));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(deviceMapper.mapToDTO(any(Device.class))).thenReturn(new DeviceDTO());

        CalibrateDeviceDTO dto = new CalibrateDeviceDTO();
        dto.setAirTemperatureOffset(1.5f);

        deviceService.calibrateDevice("device-1", dto);

        ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
        verify(deviceRepository).save(captor.capture());
        assertThat(captor.getValue().getAirTemperatureOffset()).isEqualTo(1.5f);
        assertThat(captor.getValue().getSoilMoistureOffset()).isEqualTo(1.0f);
    }

    @Test
    void createDevice_withValidUserId_createsDeviceWithDefaults() {
        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setUsername("tester");

        CreateDeviceDTO request = new CreateDeviceDTO();
        request.setUserId("user-1");
        request.setName("New Device");

        Device savedDevice = new Device();
        savedDevice.setId("device-1");
        savedDevice.setName("New Device");
        savedDevice.setUser(user);
        savedDevice.setStatus("offline");
        savedDevice.setStatusRelay(false);
        savedDevice.setAutoWaterEnabled(false);
        savedDevice.setCreatedAt(Instant.now());

        DeviceDTO dto = new DeviceDTO();
        dto.setId("device-1");
        dto.setName("New Device");
        dto.setStatus("offline");
        dto.setAutoWaterEnabled(false);

        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(deviceMapper.mapToDTO(any(Device.class))).thenReturn(dto);

        DeviceDTO result = deviceService.createDevice(request);

        assertThat(result.getStatus()).isEqualTo("offline");
        assertThat(result.getAutoWaterEnabled()).isFalse();
        assertThat(result.getName()).isEqualTo("New Device");
        verify(auditLogService).logAction(eq("CREATE_DEVICE"), isNull(), anyString());
    }

    @Test
    void createDevice_whenUserNotFound_throwsResourceNotFoundException() {
        CreateDeviceDTO request = new CreateDeviceDTO();
        request.setUserId("missing-user");
        request.setName("New Device");

        when(userRepository.findById("missing-user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.createDevice(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
