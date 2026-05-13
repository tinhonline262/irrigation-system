package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.*;
import com.irrigation_system.iot.service.WateringScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices/{deviceId}/schedules")
@RequiredArgsConstructor
public class WateringScheduleController {

    private final WateringScheduleService wateringScheduleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WateringScheduleDTO>>> listSchedules(
            @PathVariable String deviceId,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest request
    ) {
        String requesterUserId = jwt.getClaimAsString("id");
        boolean isAdmin = isAdmin();

        List<WateringScheduleDTO> schedules = wateringScheduleService.listSchedules(deviceId, requesterUserId, isAdmin);
        return ResponseEntity.ok(ApiResponse.<List<WateringScheduleDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Schedules retrieved successfully")
                .path(request.getRequestURI())
                .data(schedules)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WateringScheduleDTO>> createSchedule(
            @PathVariable String deviceId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateWateringScheduleDTO dto,
            HttpServletRequest request
    ) {
        String requesterUserId = jwt.getClaimAsString("id");
        boolean isAdmin = isAdmin();

        WateringScheduleDTO created = wateringScheduleService.createSchedule(deviceId, requesterUserId, isAdmin, dto);
        return new ResponseEntity<>(ApiResponse.<WateringScheduleDTO>builder()
                .status(HttpStatus.CREATED.value())
                .message("Schedule created successfully")
                .path(request.getRequestURI())
                .data(created)
                .build(), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WateringScheduleDTO>> updateSchedule(
            @PathVariable String deviceId,
            @PathVariable("id") String scheduleId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateWateringScheduleDTO dto,
            HttpServletRequest request
    ) {
        String requesterUserId = jwt.getClaimAsString("id");
        boolean isAdmin = isAdmin();

        WateringScheduleDTO updated = wateringScheduleService.updateSchedule(deviceId, scheduleId, requesterUserId, isAdmin, dto);
        return ResponseEntity.ok(ApiResponse.<WateringScheduleDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Schedule updated successfully")
                .path(request.getRequestURI())
                .data(updated)
                .build());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<WateringScheduleDTO>> toggleSchedule(
            @PathVariable String deviceId,
            @PathVariable("id") String scheduleId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ToggleScheduleDTO dto,
            HttpServletRequest request
    ) {
        String requesterUserId = jwt.getClaimAsString("id");
        boolean isAdmin = isAdmin();

        WateringScheduleDTO updated = wateringScheduleService.toggleSchedule(deviceId, scheduleId, requesterUserId, isAdmin, dto);
        return ResponseEntity.ok(ApiResponse.<WateringScheduleDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Schedule toggled successfully")
                .path(request.getRequestURI())
                .data(updated)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable String deviceId,
            @PathVariable("id") String scheduleId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String requesterUserId = jwt.getClaimAsString("id");
        boolean isAdmin = isAdmin();

        wateringScheduleService.deleteSchedule(deviceId, scheduleId, requesterUserId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}

