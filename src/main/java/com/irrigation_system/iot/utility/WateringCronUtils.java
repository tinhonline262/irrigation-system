package com.irrigation_system.iot.utility;

import org.springframework.scheduling.support.CronExpression;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Cron helpers for watering schedules.
 * <p>
 * <b>System uses skip-missed execution semantics.</b> {@link #computeNextRunAt(String)}
 * always anchors on {@code now} (via {@link ZonedDateTime#now()}), not on a previously
 * missed {@code nextRunAt}. Late or down-time runs do not replay skipped slots.
 */
public final class WateringCronUtils {

    private WateringCronUtils() {
    }

    /**
     * Next fire time after the current instant for a 5-field cron (minute hour dom month dow).
     * Uses the JVM default time zone. Returns {@code null} if no next occurrence exists.
     */
    public static Instant computeNextRunAt(String cron5) {
        CronExpression expression = CronExpression.parse("0 " + cron5);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime next = expression.next(now);
        return next != null ? next.toInstant() : null;
    }
}
