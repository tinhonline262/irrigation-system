package com.irrigation_system.iot.utility;

import com.irrigation_system.iot.properties.IrrigationScheduleProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class IrrigationSchedulePropertiesTest {

    @Test
    void computeWateringDuration_usesDoubleDivision_notIntegerTruncation() {
        IrrigationScheduleProperties props = new IrrigationScheduleProperties();
        props.setFlowRateMlPerMinute(100);
        props.setMinDurationSeconds(10);
        props.setMaxDurationMinutes(120);

        Duration duration = props.computeWateringDuration(50f);

        assertThat(duration.getSeconds()).isEqualTo(30);
    }

    @Test
    void computeWateringDuration_clampsToMinimum() {
        IrrigationScheduleProperties props = new IrrigationScheduleProperties();
        props.setFlowRateMlPerMinute(1000);
        props.setMinDurationSeconds(10);
        props.setMaxDurationMinutes(120);

        Duration duration = props.computeWateringDuration(1f);

        assertThat(duration.getSeconds()).isEqualTo(10);
    }
}
