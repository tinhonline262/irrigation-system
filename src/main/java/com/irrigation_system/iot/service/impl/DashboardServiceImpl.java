package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.*;
import com.irrigation_system.iot.entity.AirSensorReading;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.SoilSensorReading;
import com.irrigation_system.iot.repository.AirSensorReadingRepository;
import com.irrigation_system.iot.repository.DeviceRepository;
import com.irrigation_system.iot.repository.SoilSensorReadingRepository;
import com.irrigation_system.iot.repository.WateringLogRepository;
import com.irrigation_system.iot.repository.UserRepository;
import com.irrigation_system.iot.utility.AuthenticationUtils;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DeviceRepository deviceRepository;
    private final SoilSensorReadingRepository soilSensorReadingRepository;
    private final AirSensorReadingRepository airSensorReadingRepository;
    private final WateringLogRepository wateringLogRepository;
    private final UserRepository userRepository;

    @Override
    public DashboardSummaryDTO getDashboardSummary(String deviceId) {
        verifyDeviceOwnership(deviceId);
        return getDashboardSummaryInternal(deviceId);
    }

    @Override
    public DashboardSummaryDTO getDashboardSummaryInternal(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        Optional<SoilSensorReading> latestSoil = soilSensorReadingRepository.findFirstByDeviceIdOrderByRecordedAtDesc(deviceId);
        Optional<AirSensorReading> latestAir = airSensorReadingRepository.findFirstByDeviceIdOrderByRecordedAtDesc(deviceId);

        Instant startOfDay = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        Float totalWaterAmount = wateringLogRepository.sumWaterAmountMlByDeviceIdAndStartedAtAfter(deviceId, startOfDay);
        if (totalWaterAmount == null) {
            totalWaterAmount = 0f;
        }

        return DashboardSummaryDTO.builder()
                .deviceId(deviceId)
                .status(device.getStatus())
                .statusRelay(device.getStatusRelay())
                .latestSoilMoisturePercent(latestSoil.map(SoilSensorReading::getMoisturePercent).orElse(null))
                .latestTemperatureCelsius(latestAir.map(AirSensorReading::getTemperatureCelsius).orElse(null))
                .latestHumidityPercent(latestAir.map(AirSensorReading::getHumidityPercent).orElse(null))
                .totalWaterAmountMlToday(totalWaterAmount)
                .build();
    }

    private Device verifyDeviceOwnership(String deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Device not found"));

        String currentUsername;
        try {
            currentUsername = AuthenticationUtils.getCurrentUsername();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }

        UserEntity currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (device.getOwnerId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This device does not have an owner assigned (owner_id is null)");
        }
        
        if (!device.getOwnerId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this device. Owner ID does not match your User ID.");
        }

        return device;
    }

    @Override
    public SoilSensorReadingDTO getLatestSoilSensorReading(String deviceId) {
        // Verify device exists and enforce ownership
        verifyDeviceOwnership(deviceId);

        Optional<SoilSensorReading> latestSoil = soilSensorReadingRepository.findFirstByDeviceIdOrderByRecordedAtDesc(deviceId);

        return latestSoil.map(reading -> SoilSensorReadingDTO.builder()
                .deviceId(deviceId)
                .moisturePercent(reading.getMoisturePercent())
                .recordedAt(reading.getRecordedAt())
                .build())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No soil sensor readings found for this device"));
    }

    @Override
    public List<SoilSensorHistoryDTO> getSoilSensorHistory(String deviceId, Instant startDate, Instant endDate, String interval) {
        // Verify device exists and enforce ownership
        verifyDeviceOwnership(deviceId);

        List<SoilSensorReading> readings = soilSensorReadingRepository.findRawHistory(deviceId, startDate, endDate);

        if (readings.isEmpty()) {
            return new ArrayList<>();
        }

        switch (interval.toUpperCase()) {
            case "RAW":
                return readings.stream()
                        .map(reading -> SoilSensorHistoryDTO.builder()
                                .timestamp(reading.getRecordedAt())
                                .avgMoisturePercent(reading.getMoisturePercent())
                                .build())
                        .collect(Collectors.toList());
            case "HOURLY":
                return aggregateByInterval(readings, ChronoUnit.HOURS);
            case "DAILY":
                return aggregateByInterval(readings, ChronoUnit.DAYS);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid interval. Use RAW, HOURLY, or DAILY");
        }
    }

    private List<SoilSensorHistoryDTO> aggregateByInterval(List<SoilSensorReading> readings, ChronoUnit unit) {
        Map<Instant, List<Float>> grouped = readings.stream()
                .collect(Collectors.groupingBy(
                        reading -> {
                            LocalDateTime dateTime = LocalDateTime.ofInstant(reading.getRecordedAt(), ZoneId.systemDefault());
                            if (unit == ChronoUnit.HOURS) {
                                dateTime = dateTime.truncatedTo(ChronoUnit.HOURS);
                            } else if (unit == ChronoUnit.DAYS) {
                                dateTime = dateTime.toLocalDate().atStartOfDay();
                            }
                            return dateTime.atZone(ZoneId.systemDefault()).toInstant();
                        },
                        LinkedHashMap::new,
                        Collectors.mapping(SoilSensorReading::getMoisturePercent, Collectors.toList())
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    float avg = (float) entry.getValue().stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
                    return SoilSensorHistoryDTO.builder()
                            .timestamp(entry.getKey())
                            .avgMoisturePercent(avg)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public AirSensorReadingDTO getLatestAirSensorReading(String deviceId) {
        // Verify device exists and enforce ownership
        verifyDeviceOwnership(deviceId);

        Optional<AirSensorReading> latestAir = airSensorReadingRepository.findFirstByDeviceIdOrderByRecordedAtDesc(deviceId);

        return latestAir.map(reading -> AirSensorReadingDTO.builder()
                .deviceId(deviceId)
                .temperatureCelsius(reading.getTemperatureCelsius())
                .humidityPercent(reading.getHumidityPercent())
                .recordedAt(reading.getRecordedAt())
                .build())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No air sensor readings found for this device"));
    }

    @Override
    public List<AirSensorHistoryDTO> getAirSensorHistory(String deviceId, Instant startDate, Instant endDate, String interval) {
        // Verify device exists and enforce ownership
        verifyDeviceOwnership(deviceId);

        List<AirSensorReading> readings = airSensorReadingRepository.findRawHistory(deviceId, startDate, endDate);

        if (readings.isEmpty()) {
            return new ArrayList<>();
        }

        switch (interval.toUpperCase()) {
            case "RAW":
                return readings.stream()
                        .map(reading -> AirSensorHistoryDTO.builder()
                                .timestamp(reading.getRecordedAt())
                                .avgTemperatureCelsius(reading.getTemperatureCelsius())
                                .avgHumidityPercent(reading.getHumidityPercent())
                                .build())
                        .collect(Collectors.toList());
            case "HOURLY":
                return aggregateAirByInterval(readings, ChronoUnit.HOURS);
            case "DAILY":
                return aggregateAirByInterval(readings, ChronoUnit.DAYS);
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid interval. Use RAW, HOURLY, or DAILY");
        }
    }

    private List<AirSensorHistoryDTO> aggregateAirByInterval(List<AirSensorReading> readings, ChronoUnit unit) {
        Map<Instant, List<AirSensorReading>> grouped = readings.stream()
                .collect(Collectors.groupingBy(
                        reading -> {
                            LocalDateTime dateTime = LocalDateTime.ofInstant(reading.getRecordedAt(), ZoneId.systemDefault());
                            if (unit == ChronoUnit.HOURS) {
                                dateTime = dateTime.truncatedTo(ChronoUnit.HOURS);
                            } else if (unit == ChronoUnit.DAYS) {
                                dateTime = dateTime.toLocalDate().atStartOfDay();
                            }
                            return dateTime.atZone(ZoneId.systemDefault()).toInstant();
                        },
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<AirSensorReading> group = entry.getValue();
                    double avgTemp = group.stream().mapToDouble(AirSensorReading::getTemperatureCelsius).average().orElse(0.0);
                    double avgHumidity = group.stream().mapToDouble(AirSensorReading::getHumidityPercent).average().orElse(0.0);
                    return AirSensorHistoryDTO.builder()
                            .timestamp(entry.getKey())
                            .avgTemperatureCelsius((float) avgTemp)
                            .avgHumidityPercent((float) avgHumidity)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public SoilSensorStatsDTO getSoilSensorStats(String deviceId) {
        // Verify device exists and enforce ownership
        verifyDeviceOwnership(deviceId);

        Instant now = Instant.now();

        // Calculate start dates for each period
        Instant last24Hours = now.minus(24, ChronoUnit.HOURS);
        Instant last7Days = now.minus(7, ChronoUnit.DAYS);
        Instant last30Days = now.minus(30, ChronoUnit.DAYS);

        // Get stats for each period
        SoilSensorStatsDTO.PeriodStats stats24h = getPeriodStats(deviceId, last24Hours);
        SoilSensorStatsDTO.PeriodStats stats7d = getPeriodStats(deviceId, last7Days);
        SoilSensorStatsDTO.PeriodStats stats30d = getPeriodStats(deviceId, last30Days);

        return SoilSensorStatsDTO.builder()
                .last24Hours(stats24h)
                .last7Days(stats7d)
                .last30Days(stats30d)
                .build();
    }

    private SoilSensorStatsDTO.PeriodStats getPeriodStats(String deviceId, Instant startDate) {
        Object[] result = soilSensorReadingRepository.getStatsByDeviceIdAndRecordedAtAfter(deviceId, startDate);

        Float min = ((Number) result[0]).floatValue();
        Float max = ((Number) result[1]).floatValue();
        Float avg = ((Number) result[2]).floatValue();

        return SoilSensorStatsDTO.PeriodStats.builder()
                .minMoisturePercent(min)
                .maxMoisturePercent(max)
                .avgMoisturePercent(avg)
                .build();
    }
}
