package com.irrigation_system.iot.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WateringLogPageDTO {
    String deviceId;
    int page;
    int size;
    long totalElements;
    int totalPages;
    List<WateringLogDTO> logs;
}
