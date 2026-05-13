package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateWateringScheduleDTO {

    @NotBlank(message = "Cron expression is required")
    private String cronExpression;

    @NotNull(message = "Water amount is required")
    @Positive(message = "Water amount must be > 0")
    private Float waterAmountMl;

    private Boolean enabled;
}

