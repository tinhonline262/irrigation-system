package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UpdateWateringScheduleDTO {

    private String cronExpression;

    @Positive(message = "Duration must be > 0")
    private Long durationInMinutes;

    private Boolean enabled;
}

