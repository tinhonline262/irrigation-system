package com.irrigation_system.iot.config;

import com.irrigation_system.iot.entity.RoleEntity;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.repository.RoleRepository;
import com.irrigation_system.iot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class ApplicationInit {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            log.info("Checking if admin account exists...");

            // Ensure ADMIN role exists
            String adminRoleName = "ADMIN";
            RoleEntity adminRole = roleRepository.findByName(adminRoleName).orElseGet(() -> {
                log.info("Role {} not found, creating it...", adminRoleName);
                RoleEntity newRole = new RoleEntity();
                newRole.setName(adminRoleName);
                return roleRepository.save(newRole);
            });

            // Check if admin user already exists
            String adminUsername = "admin";
            Optional<UserEntity> existingAdmin = userRepository.findByUsername(adminUsername);

            if (existingAdmin.isEmpty()) {
                log.info("Admin account not found. Creating default admin account...");
                UserEntity adminUser = new UserEntity();
                adminUser.setUsername(adminUsername);
                adminUser.setEmail("admin@example.com");
                adminUser.setName("System Admin");
                adminUser.setPassword(passwordEncoder.encode("admin123")); // Default password
                adminUser.setRoles(new HashSet<>(roleRepository.findAllByNameIn(List.of(adminRoleName))));
                
                userRepository.save(adminUser);
                log.info("Default admin account created successfully. Username: admin, Password: admin123");
            } else {
                log.info("Admin account already exists. Skipping initialization.");
            }
        };
    }
}
