package com.irrigation_system.iot.mapper;

import com.irrigation_system.iot.dto.DeviceDTO;
import com.irrigation_system.iot.entity.Device;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    DeviceDTO mapToDTO(Device entity);

}