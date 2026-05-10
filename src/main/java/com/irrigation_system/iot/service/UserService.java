package com.irrigation_system.iot.service;


import com.irrigation_system.iot.dto.UpdateUserRolesDTO;
import com.irrigation_system.iot.dto.UserProfileDTO;
import com.irrigation_system.iot.entity.SignUpEntity;
import com.irrigation_system.iot.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserProfileDTO getProfileByUsername(String username);

    UserEntity getUserByUsername(String username);

    void createUser(SignUpEntity signUpEntity);

    void deleteUser(String username);
    
    Page<UserProfileDTO> getAllUsers(Pageable pageable);
    
    UserProfileDTO updateUserRoles(String id, UpdateUserRolesDTO adminUpdateUserRolesDTO);

    void deleteUserById(String id);

    void resetPassword(String id, String newPassword);
}
