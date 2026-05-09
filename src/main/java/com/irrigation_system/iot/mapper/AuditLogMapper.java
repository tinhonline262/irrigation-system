package com.irrigation_system.iot.mapper;

import com.irrigation_system.iot.dto.AuditLogDto;
import com.irrigation_system.iot.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    AuditLogDto toDto(AuditLog entity);
}
