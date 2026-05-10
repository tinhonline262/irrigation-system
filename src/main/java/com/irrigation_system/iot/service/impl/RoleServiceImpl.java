package com.irrigation_system.iot.service.impl;

import com.irrigation_system.iot.dto.CreatePermissionDTO;
import com.irrigation_system.iot.dto.CreateRoleDTO;
import com.irrigation_system.iot.dto.PermissionDTO;
import com.irrigation_system.iot.dto.RoleDTO;
import com.irrigation_system.iot.entity.PermissionEntity;
import com.irrigation_system.iot.entity.RoleEntity;
import com.irrigation_system.iot.exception.ResourceAlreadyExistsException;
import com.irrigation_system.iot.exception.ResourceNotFoundException;
import com.irrigation_system.iot.mapper.PermissionMapper;
import com.irrigation_system.iot.mapper.RoleMapper;
import com.irrigation_system.iot.repository.PermissionRepository;
import com.irrigation_system.iot.repository.RoleRepository;
import com.irrigation_system.iot.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public RoleEntity getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", name));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleDTO> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable).map(roleMapper::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PermissionDTO> getAllPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable).map(permissionMapper::mapToDTO);
    }

    @Override
    public PermissionDTO createPermission(CreatePermissionDTO createPermissionDTO) {
        String permissionName = createPermissionDTO.getName().toUpperCase();
        if (permissionRepository.existsByName(permissionName)) {
            throw new ResourceAlreadyExistsException("Permission", "name", permissionName);
        }

        PermissionEntity permission = new PermissionEntity();
        permission.setName(permissionName);
        permission.setDescription(createPermissionDTO.getDescription());
        
        permission = permissionRepository.save(permission);
        return permissionMapper.mapToDTO(permission);
    }

    @Override
    public RoleDTO createRole(CreateRoleDTO createRoleDTO) {
        List<PermissionEntity> permissions = permissionRepository.findAllByNameIn(createRoleDTO.getPermissions());
        
        RoleEntity role = new RoleEntity();
        role.setName(createRoleDTO.getName().toUpperCase());
        role.setPermissions(new HashSet<>(permissions));
        
        role = roleRepository.save(role);
        return roleMapper.mapToDTO(role);
    }

    @Override
    public RoleDTO updateRolePermissions(String id, List<String> permissions) {
        RoleEntity role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", id));
                
        List<PermissionEntity> permissionEntities = permissionRepository.findAllByNameIn(permissions);
        role.setPermissions(new HashSet<>(permissionEntities));
        
        role = roleRepository.save(role);
        return roleMapper.mapToDTO(role);
    }
}
