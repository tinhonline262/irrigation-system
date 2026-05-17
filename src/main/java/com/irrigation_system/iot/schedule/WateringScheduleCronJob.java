package com.irrigation_system.iot.schedule;

import com.irrigation_system.iot.service.ScheduledWateringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WateringScheduleCronJob {

    private final ScheduledWateringService scheduledWateringService;

    // Not safe for horizontal scaling without distributed lock (e.g. ShedLock).
    // Running multiple containers may execute the same schedule twice.
    @Scheduled(cron = "${app.irrigation.schedule-cron:0 * * * * *}", zone = "Asia/Ho_Chi_Minh")
    public void runDueSchedules() {
        try {
            scheduledWateringService.processDueSchedules();
        } catch (Exception ex) {
            log.error("ERROR: Scheduled watering job failed to process due schedules", ex);
        }
    }
}
