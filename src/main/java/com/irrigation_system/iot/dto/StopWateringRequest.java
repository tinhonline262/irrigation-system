package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StopWateringRequest {
    @Positive(message = "Water amount must be greater than 0")
    Float waterAmountMl;
}
