package com.irrigation_system.iot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StopWateringRequest {
    @NotNull
    Float waterAmountMl;
}
