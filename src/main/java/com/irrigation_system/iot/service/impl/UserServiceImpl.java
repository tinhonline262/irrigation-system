package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.UserProfileDTO;
import com.irrigation_system.iot.entity.RoleEntity;
import com.irrigation_system.iot.entity.SignUpEntity;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.enumeration.UserDefaultType;
import com.irrigation_system.iot.exception.ResourceAlreadyExistsException;
import com.irrigation_system.iot.exception.ResourceNotFoundException;
import com.irrigation_system.iot.mapper.UserMapper;
import com.irrigation_system.iot.repository.UserRepository;
import com.irrigation_system.iot.service.RoleService;
import com.irrigation_system.iot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleService roleService;

    @Override
    public UserProfileDTO getProfileByUsername(String username) {
        log.info("Getting profile for username: {}", username);
        UserEntity userEntity = getUserEntity(username);
        return userMapper.mapToProfileDTO(userEntity);
    }

    @Override
    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username '%s' not found".
                        formatted(username)));
    }

    @Override
    public void createUser(SignUpEntity signUpEntity) {
        if (userRepository.existsByUsername(signUpEntity.getUsername())) {
            throw new ResourceAlreadyExistsException("User is already existed");
        }
        if (userRepository.existsByEmail(signUpEntity.getEmail())) {
            throw new ResourceAlreadyExistsException("Email is already existed");
        }

        // Save user info to database
        UserEntity userEntity = userMapper.mapToEntity(signUpEntity);
        RoleEntity userRole = roleService.getRoleByName(UserDefaultType.USER.name());
        userEntity.setRoles(Set.of(userRole));
        userEntity.setSignUp(signUpEntity);
        userRepository.save(userEntity);
    }

    @Override
    public void deleteUser(String username) {
        log.info("Deleting user with username: {}", username);
        UserEntity userEntity = getUserEntity(username);
        // Soft delete
        userRepository.delete(userEntity);
        log.info("User {} deleted (soft delete)", username);
    }

    private UserEntity getUserEntity(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("User", "username", username)
        );
    }
}
