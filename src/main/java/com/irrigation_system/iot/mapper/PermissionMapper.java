package com.irrigation_system.iot.mapper;

import com.irrigation_system.iot.dto.PermissionDTO;
import com.irrigation_system.iot.entity.PermissionEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDTO mapToDTO(PermissionEntity entity);
}