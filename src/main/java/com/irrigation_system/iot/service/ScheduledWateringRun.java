package com.irrigation_system.iot.service;

import java.time.Duration;

/**
 * Result of starting a scheduled watering run (used to schedule in-memory OFF after commit).
 */
public record ScheduledWateringRun(
        String wateringLogId,
        String chipId,
        float waterAmountMl,
        Duration duration
) {
}
