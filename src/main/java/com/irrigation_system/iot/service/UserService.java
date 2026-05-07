package com.irrigation_system.iot.service;


import com.irrigation_system.iot.dto.UserProfileDTO;
import com.irrigation_system.iot.entity.SignUpEntity;
import com.irrigation_system.iot.entity.UserEntity;

public interface UserService {

    UserProfileDTO getProfileByUsername(String username);

    UserEntity getUserByUsername(String username);

    void createUser(SignUpEntity signUpEntity);

    void deleteUser(String id);
}
