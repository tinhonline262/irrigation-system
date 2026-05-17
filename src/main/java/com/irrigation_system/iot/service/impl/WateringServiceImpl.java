package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.WateringLogDTO;
import com.irrigation_system.iot.dto.WateringLogPageDTO;
import com.irrigation_system.iot.dto.WateringLogStatsDTO;
import com.irrigation_system.iot.dto.WateringStatusDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.entity.WateringLog;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.repository.UserRepository;
import com.irrigation_system.iot.repository.WateringLogRepository;
import com.irrigation_system.iot.service.WateringNotificationService;
import com.irrigation_system.iot.service.WateringService;
import com.irrigation_system.iot.properties.IrrigationScheduleProperties;
import com.irrigation_system.iot.utility.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class WateringServiceImpl implements WateringService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final WateringLogRepository wateringLogRepository;
    private final WateringNotificationService wateringNotificationService;
    private final IrrigationScheduleProperties irrigationProperties;

    @Override
    public DeviceAndUser verifyDeviceOwnership(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        String username;
        try {
            username = AuthenticationUtils.getCurrentUsername();
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found"));

        if (device.getOwnerId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This device does not have an owner assigned (owner_id is null)");
        }

        if (!user.getId().equals(device.getOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this device. Owner ID does not match your User ID.");
        }

        return new DeviceAndUser(device, user);
    }

    @Override
    public WateringLogDTO startManualWatering(String deviceId) {
        DeviceAndUser validation = verifyDeviceOwnership(deviceId);
        Device device = validation.device;
        UserEntity user = validation.user;

        WateringLog wateringLog = new WateringLog();
        wateringLog.setId(UUID.randomUUID().toString());
        wateringLog.setDevice(device);
        wateringLog.setTriggeredBy(user);
        wateringLog.setTriggerType("manual");
        wateringLog.setStartedAt(Instant.now());

        WateringLog savedLog = wateringLogRepository.save(wateringLog);

        return WateringLogDTO.builder()
                .id(savedLog.getId())
                .deviceId(device.getId())
                .triggeredBy(user.getUsername())
                .triggerType(savedLog.getTriggerType())
                .startedAt(savedLog.getStartedAt())
                .endedAt(savedLog.getEndedAt())
                .waterAmountMl(savedLog.getWaterAmountMl())
                .build();
    }

    @Override
    public WateringLogDTO stopManualWatering(String deviceId) {
        DeviceAndUser validation = verifyDeviceOwnership(deviceId);
        Device device = validation.device;

        WateringLog wateringLog = wateringLogRepository
                .findFirstByDevice_IdAndTriggerTypeAndEndedAtIsNullOrderByStartedAtDesc(device.getId(), "manual")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active manual watering session found for this device"));

        Instant now = Instant.now();
        wateringLog.setEndedAt(now);

        long durationSeconds = Duration.between(wateringLog.getStartedAt(), now).getSeconds();
        if (durationSeconds < 0) durationSeconds = 0;
        
        double mlPerSecond = irrigationProperties.getFlowRateMlPerMinute() / 60.0;
        float waterAmountMl = (float) (durationSeconds * mlPerSecond);
        
        wateringLog.setWaterAmountMl(waterAmountMl);

        WateringLog savedLog = wateringLogRepository.save(wateringLog);

        wateringNotificationService.notifyWateringCompleted(device, waterAmountMl, false);

        return WateringLogDTO.builder()
                .id(savedLog.getId())
                .deviceId(savedLog.getDevice().getId())
                .triggeredBy(savedLog.getTriggeredBy() != null ? savedLog.getTriggeredBy().getUsername() : null)
                .triggerType(savedLog.getTriggerType())
                .startedAt(savedLog.getStartedAt())
                .endedAt(savedLog.getEndedAt())
                .waterAmountMl(savedLog.getWaterAmountMl())
                .build();
    }

    @Override
    public WateringStatusDTO getWateringStatus(String deviceId) {
        verifyDeviceOwnership(deviceId);

        return wateringLogRepository.findFirstByDevice_IdAndEndedAtIsNullOrderByStartedAtDesc(deviceId)
                .map(wateringLog -> {
                    long elapsedSeconds = Duration.between(wateringLog.getStartedAt(), Instant.now()).getSeconds();
                    return WateringStatusDTO.builder()
                            .deviceId(deviceId)
                            .running(true)
                            .wateringLogId(wateringLog.getId())
                            .triggerType(wateringLog.getTriggerType())
                            .startedAt(wateringLog.getStartedAt())
                            .elapsedSeconds(elapsedSeconds)
                            .build();
                })
                .orElseGet(() -> WateringStatusDTO.builder()
                        .deviceId(deviceId)
                        .running(false)
                        .build());
    }

    @Override
    public WateringLogPageDTO getWateringLogs(String deviceId, int page, int size) {
        verifyDeviceOwnership(deviceId);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
        Page<WateringLog> logPage = wateringLogRepository.findByDevice_IdOrderByStartedAtDesc(deviceId, pageRequest);

        List<WateringLogDTO> logs = logPage.stream()
                .map(log -> WateringLogDTO.builder()
                        .id(log.getId())
                        .deviceId(log.getDevice().getId())
                        .triggeredBy(log.getTriggeredBy() != null ? log.getTriggeredBy().getUsername() : null)
                        .triggerType(log.getTriggerType())
                        .startedAt(log.getStartedAt())
                        .endedAt(log.getEndedAt())
                        .waterAmountMl(log.getWaterAmountMl())
                        .build())
                .collect(Collectors.toList());

        return WateringLogPageDTO.builder()
                .deviceId(deviceId)
                .page(logPage.getNumber())
                .size(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .logs(logs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public void exportWateringLogsCsv(String deviceId, OutputStream outputStream) {
        verifyDeviceOwnership(deviceId);

        try (Stream<WateringLog> wateringLogs = wateringLogRepository.findByDevice_IdOrderByStartedAtDesc(deviceId);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {

            // UTF-8 BOM — required for Excel to correctly open Vietnamese/special characters
            writer.write('\uFEFF');
            writer.write("id,deviceId,triggeredBy,triggerType,startedAt,endedAt,waterAmountMl");
            writer.newLine();

            wateringLogs.forEach(log -> {
                try {
                    writer.write(String.join(",",
                            csvEscape(log.getId()),
                            csvEscape(log.getDevice().getId()),
                            csvEscape(log.getTriggeredBy() != null ? log.getTriggeredBy().getUsername() : ""),
                            csvEscape(log.getTriggerType()),
                            csvEscape(log.getStartedAt() != null ? log.getStartedAt().toString() : ""),
                            csvEscape(log.getEndedAt() != null ? log.getEndedAt().toString() : ""),
                            csvEscape(log.getWaterAmountMl() != null ? log.getWaterAmountMl().toString() : "")
                    ));
                    writer.newLine();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
            writer.flush();
        } catch (UncheckedIOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export watering logs", ex.getCause());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to export watering logs", ex);
        }
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    @Override
    public List<WateringLogStatsDTO> getWateringLogStats(String deviceId) {
        verifyDeviceOwnership(deviceId);

        return wateringLogRepository.findDailyWateringStatsByDeviceId(deviceId).stream()
                .map(row -> WateringLogStatsDTO.builder()
                        .date(row[0] != null ? row[0].toString() : null)
                        .totalWaterAmountMl(row[1] != null ? ((Number) row[1]).floatValue() : 0f)
                        .wateringCount(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                        .build())
                .collect(Collectors.toList());
    }
}
