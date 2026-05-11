package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StopWateringRequest {
    @NotNull(message = "Water amount is required")
    @Positive(message = "Water amount must be greater than 0")
    Float waterAmountMl;
}
