package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.entity.RoleEntity;
import com.irrigation_system.iot.exception.ResourceNotFoundException;
import com.irrigation_system.iot.repository.RoleRepository;
import com.irrigation_system.iot.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public RoleEntity getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role %s not found".formatted(name)));
    }
}
