package com.irrigation_system.iot.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "app.irrigation")
@Data
public class IrrigationScheduleProperties {

    private String scheduleCron = "0 * * * * *";
    private double flowRateMlPerMinute = 100;
    private long minDurationSeconds = 10;
    private long maxDurationMinutes = 120;

    /**
     * Estimated watering duration from target volume and configured flow rate.
     */
    public Duration computeWateringDuration(float waterAmountMl) {
        double minutes = waterAmountMl / flowRateMlPerMinute;
        long seconds = Math.round(minutes * 60);
        long maxSeconds = maxDurationMinutes * 60;
        long clamped = Math.clamp(seconds, minDurationSeconds, maxSeconds);
        return Duration.ofSeconds(clamped);
    }
}
