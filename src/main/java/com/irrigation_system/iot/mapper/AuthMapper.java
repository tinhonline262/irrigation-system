package com.irrigation_system.iot.mapper;

import com.irrigation_system.iot.dto.LoginResponseDTO;
import com.irrigation_system.iot.dto.RegistrationDTO;
import com.irrigation_system.iot.entity.SignUpEntity;
import com.irrigation_system.iot.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(uses = {RoleMapper.class})
public interface AuthMapper {

    SignUpEntity map(RegistrationDTO registrationDTO, @MappingTarget SignUpEntity signUpEntity);

    LoginResponseDTO map(UserEntity user);
}
