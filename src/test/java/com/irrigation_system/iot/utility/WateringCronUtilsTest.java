package com.irrigation_system.iot.utility;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class WateringCronUtilsTest {

    @Test
    void computeNextRunAt_returnsInstantAfterNow() {
        Instant now = Instant.now();
        Instant next = WateringCronUtils.computeNextRunAt("0 6 * * *");
        assertThat(next).isNotNull();
        assertThat(next).isAfter(now);
    }

    @Test
    void computeNextRunAt_skipMissed_anchorsFromNowNotPastSlot() {
        // Daily at 06:00 — next run is always the next 06:00 after now, never a past slot
        Instant next = WateringCronUtils.computeNextRunAt("0 6 * * *");
        assertThat(next).isAfter(Instant.now());
    }
}
