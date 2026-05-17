package com.irrigation_system.iot.utility;

import com.irrigation_system.iot.repository.AirSensorReadingRepository;
import com.irrigation_system.iot.repository.SoilSensorReadingRepository;
import com.irrigation_system.iot.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataRetentionCronJob {

    private final SystemConfigService systemConfigService;
    private final SoilSensorReadingRepository soilSensorReadingRepository;
    private final AirSensorReadingRepository airSensorReadingRepository;

//    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Ho_Chi_Minh") // Run every day at 2 AM
    @Transactional
    public void purgeOldSensorData() {
        log.info("Starting data retention purge cron job...");
        int retentionDays;
        try {
            String retentionStr = systemConfigService.getConfigValue("retention", "30"); // Default 30 days
            retentionDays = Integer.parseInt(retentionStr);
        } catch (NumberFormatException e) {
            log.error("Invalid retention config value, defaulting to 30 days");
            retentionDays = 30;
        }

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);

        int deletedSoilRows = soilSensorReadingRepository.deleteByCreatedAtBefore(cutoffDate);
        int deletedAirRows = airSensorReadingRepository.deleteByCreatedAtBefore(cutoffDate);

        log.info("Finished data retention purge. Deleted {} soil records and {} air records before {}",
                deletedSoilRows, deletedAirRows, cutoffDate);
    }
}
