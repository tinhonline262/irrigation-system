package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.CreatePermissionDTO;
import com.irrigation_system.iot.dto.CreateRoleDTO;
import com.irrigation_system.iot.dto.PermissionDTO;
import com.irrigation_system.iot.dto.RoleDTO;
import com.irrigation_system.iot.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {

    RoleEntity getRoleByName(String name);

    Page<RoleDTO> getAllRoles(Pageable pageable);

    Page<PermissionDTO> getAllPermissions(Pageable pageable);

    PermissionDTO createPermission(CreatePermissionDTO createPermissionDTO);

    RoleDTO createRole(CreateRoleDTO createRoleDTO);

    RoleDTO updateRolePermissions(String id, List<String> permissions);
}
