package com.irrigation_system.iot.mapper;

import com.irrigation_system.iot.dto.UserProfileDTO;
import com.irrigation_system.iot.entity.SignUpEntity;
import com.irrigation_system.iot.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(uses = {RoleMapper.class}, componentModel = "spring")
public interface UserMapper {

    UserProfileDTO mapToProfileDTO(UserEntity entity);
    UserEntity mapToEntity(SignUpEntity signUpEntity);
}
