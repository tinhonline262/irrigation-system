package com.irrigation_system.iot.mapper;

import com.irrigation_system.iot.dto.LoginResponseDTO;
import com.irrigation_system.iot.dto.RegistrationDTO;
import com.irrigation_system.iot.entity.SignUpEntity;
import com.irrigation_system.iot.entity.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-06T19:00:53+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Amazon.com Inc.)"
)
@Component
public class AuthMapperImpl implements AuthMapper {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public SignUpEntity map(RegistrationDTO registrationDTO, SignUpEntity signUpEntity) {
        if ( registrationDTO == null ) {
            return signUpEntity;
        }

        signUpEntity.setUsername( registrationDTO.getUsername() );
        signUpEntity.setPassword( registrationDTO.getPassword() );
        signUpEntity.setName( registrationDTO.getName() );
        signUpEntity.setEmail( registrationDTO.getEmail() );

        return signUpEntity;
    }

    @Override
    public LoginResponseDTO map(UserEntity user) {
        if ( user == null ) {
            return null;
        }

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();

        loginResponseDTO.setUsername( user.getUsername() );
        loginResponseDTO.setEmail( user.getEmail() );
        loginResponseDTO.setName( user.getName() );
        loginResponseDTO.setOauthId( user.getOauthId() );
        loginResponseDTO.setRoles( roleMapper.map( user.getRoles() ) );

        return loginResponseDTO;
    }
}
