package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.UpdateUserRolesDTO;
import com.irrigation_system.iot.dto.UserProfileDTO;
import com.irrigation_system.iot.service.UserService;
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
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ_ALL')")
    public ResponseEntity<ApiResponse<Page<UserProfileDTO>>> getAllUsers(
            @PageableDefault(size = 10) Pageable pageable) {

        Page<UserProfileDTO> users = userService.getAllUsers(pageable);

        ApiResponse<Page<UserProfileDTO>> response = ApiResponse.<Page<UserProfileDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Users retrieved successfully")
                .data(users)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USER_UPDATE_ROLE')")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateUserRoles(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRolesDTO updateDto) {

        UserProfileDTO updatedUser = userService.updateUserRoles(id, updateDto);

        ApiResponse<UserProfileDTO> response = ApiResponse.<UserProfileDTO>builder()
                .status(HttpStatus.OK.value())
                .message("User roles updated successfully")
                .data(updatedUser)
                .build();

        return ResponseEntity.ok(response);
    }
}