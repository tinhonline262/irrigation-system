package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.UpdateUserRolesDTO;
import com.irrigation_system.iot.dto.UserProfileDTO;
import com.irrigation_system.iot.entity.RoleEntity;
import com.irrigation_system.iot.entity.SignUpEntity;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.enumeration.UserDefaultType;
import com.irrigation_system.iot.exception.ResourceAlreadyExistsException;
import com.irrigation_system.iot.exception.ResourceNotFoundException;
import com.irrigation_system.iot.mapper.UserMapper;
import com.irrigation_system.iot.repository.RoleRepository;
import com.irrigation_system.iot.repository.UserRepository;
import com.irrigation_system.iot.service.RoleService;
import com.irrigation_system.iot.service.UserService;
import com.irrigation_system.iot.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

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
        auditLogService.logAction("USER_DELETE", userEntity.getId(), java.util.Map.of("username", username));
        log.info("User {} deleted (soft delete)", username);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::mapToProfileDTO);
    }

    @Override
    public UserProfileDTO updateUserRoles(String id, UpdateUserRolesDTO adminUpdateUserRolesDTO) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        
        List<RoleEntity> roles = roleRepository.findAllByNameIn(adminUpdateUserRolesDTO.getRoles());
        user.setRoles(new HashSet<>(roles));
        
        user = userRepository.save(user);
        auditLogService.logAction("USER_UPDATE_ROLES", id, adminUpdateUserRolesDTO);
        return userMapper.mapToProfileDTO(user);
    }

    @Override
    public void deleteUserById(String id) {
        log.info("Deleting user with id: {}", id);
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        // Soft delete
        userRepository.delete(userEntity);
        auditLogService.logAction("USER_DELETE_BY_ID", id, java.util.Map.of("deletedId", id));
        log.info("User id {} deleted (soft delete)", id);
    }

    @Override
    public void resetPassword(String id, String newPassword) {
        log.info("Resetting password for user id: {}", id);
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity);
        auditLogService.logAction("USER_RESET_PASSWORD", id, java.util.Map.of());
    }

    private UserEntity getUserEntity(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("User", "username", username)
        );
    }
}
