package com.irrigation_system.iot.mapper;

import com.irrigation_system.iot.dto.UserProfileDTO;
import com.irrigation_system.iot.entity.SignUpEntity;
import com.irrigation_system.iot.entity.UserEntity;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-07T23:06:21+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.2 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public UserProfileDTO mapToProfileDTO(UserEntity entity) {
        if ( entity == null ) {
            return null;
        }

        UserProfileDTO userProfileDTO = new UserProfileDTO();

        userProfileDTO.setId( entity.getId() );
        userProfileDTO.setUsername( entity.getUsername() );
        userProfileDTO.setEmail( entity.getEmail() );
        userProfileDTO.setName( entity.getName() );
        userProfileDTO.setRoles( roleMapper.map( entity.getRoles() ) );
        userProfileDTO.setCreatedAt( entity.getCreatedAt() );

        return userProfileDTO;
    }

    @Override
    public UserEntity mapToEntity(SignUpEntity signUpEntity) {
        if ( signUpEntity == null ) {
            return null;
        }

        UserEntity.UserEntityBuilder userEntity = UserEntity.builder();

        userEntity.username( signUpEntity.getUsername() );
        userEntity.password( signUpEntity.getPassword() );
        userEntity.email( signUpEntity.getEmail() );
        userEntity.name( signUpEntity.getName() );

        return userEntity.build();
    }
}
