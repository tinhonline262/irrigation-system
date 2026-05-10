package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.CreatePermissionDTO;
import com.irrigation_system.iot.dto.CreateRoleDTO;
import com.irrigation_system.iot.dto.PermissionDTO;
import com.irrigation_system.iot.dto.RoleDTO;
import com.irrigation_system.iot.dto.UpdateRolePermissionsDTO;
import com.irrigation_system.iot.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_READ_ALL')")
    public ResponseEntity<ApiResponse<Page<RoleDTO>>> getAllRoles(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<RoleDTO> roles = roleService.getAllRoles(pageable);

        ApiResponse<Page<RoleDTO>> response = ApiResponse.<Page<RoleDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Roles retrieved successfully")
                .data(roles)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_READ_ALL')")
    public ResponseEntity<ApiResponse<Page<PermissionDTO>>> getAllPermissions(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<PermissionDTO> permissions = roleService.getAllPermissions(pageable);

        ApiResponse<Page<PermissionDTO>> response = ApiResponse.<Page<PermissionDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Permissions retrieved successfully")
                .data(permissions)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ApiResponse<PermissionDTO>> createPermission(
            @Valid @RequestBody CreatePermissionDTO createDto) {

        PermissionDTO createdPermission = roleService.createPermission(createDto);

        ApiResponse<PermissionDTO> response = ApiResponse.<PermissionDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("Permission created successfully")
                .data(createdPermission)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CREATE')")
    public ResponseEntity<ApiResponse<RoleDTO>> createRole(
            @Valid @RequestBody CreateRoleDTO createDto) {

        RoleDTO createdRole = roleService.createRole(createDto);

        ApiResponse<RoleDTO> response = ApiResponse.<RoleDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("Role created successfully")
                .data(createdRole)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('ROLE_UPDATE')")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRolePermissions(
            @PathVariable String id,
            @Valid @RequestBody UpdateRolePermissionsDTO updateDto) {

        RoleDTO updatedRole = roleService.updateRolePermissions(id, updateDto.getPermissions());

        ApiResponse<RoleDTO> response = ApiResponse.<RoleDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Role permissions updated successfully")
                .data(updatedRole)
                .build();

        return ResponseEntity.ok(response);
    }
}