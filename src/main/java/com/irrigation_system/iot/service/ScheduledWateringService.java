package com.irrigation_system.iot.service;

/**
 * Executes due watering schedules (cron-driven).
 */
public interface ScheduledWateringService {

    /**
     * Finds enabled schedules with {@code nextRunAt <= now} and processes each in its own transaction.
     * <p>
     * <b>System uses skip-missed execution semantics.</b> See {@link com.irrigation_system.iot.utility.WateringCronUtils}.
     */
    void processDueSchedules();

    /**
     * Idempotent completion: always sends OFF; closes log only if still open.
     */
    void completeScheduledWatering(String wateringLogId, String chipId, Float waterAmountMl);
}
