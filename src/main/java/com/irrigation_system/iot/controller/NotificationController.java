package com.irrigation_system.iot.controller;

import com.irrigation_system.iot.dto.ApiResponse;
import com.irrigation_system.iot.dto.NotificationDTO;
import com.irrigation_system.iot.dto.UnreadCountDTO;
import com.irrigation_system.iot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/v1/notifications
     * Lấy danh sách thông báo của user hiện tại (có pagination).
     */
    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public ResponseEntity<ApiResponse<Page<NotificationDTO>>> getNotifications(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(size = 10) Pageable pageable) {

        String userId = jwt.getClaimAsString("id");
        Page<NotificationDTO> notifications = notificationService.getNotifications(userId, pageable);

        ApiResponse<Page<NotificationDTO>> response = ApiResponse.<Page<NotificationDTO>>builder()
                .status(HttpStatus.OK.value())
                .message("Notifications retrieved successfully")
                .data(notifications)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/notifications/unread-count
     * Số lượng thông báo chưa đọc.
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('NOTIFICATION_READ')")
    public ResponseEntity<ApiResponse<UnreadCountDTO>> getUnreadCount(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("id");
        UnreadCountDTO unreadCount = notificationService.getUnreadCount(userId);

        ApiResponse<UnreadCountDTO> response = ApiResponse.<UnreadCountDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Unread count retrieved successfully")
                .data(unreadCount)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/v1/notifications/{id}/read
     * Đánh dấu đã đọc một thông báo.
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        String userId = jwt.getClaimAsString("id");
        NotificationDTO notification = notificationService.markAsRead(userId, id);

        ApiResponse<NotificationDTO> response = ApiResponse.<NotificationDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Notification marked as read")
                .data(notification)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/v1/notifications/read-all
     * Đánh dấu đọc tất cả thông báo.
     */
    @PatchMapping("/read-all")
    @PreAuthorize("hasAuthority('NOTIFICATION_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("id");
        notificationService.markAllAsRead(userId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("All notifications marked as read")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/v1/notifications/{id}
     * Xóa thông báo.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('NOTIFICATION_DELETE')")
    public ResponseEntity<Void> deleteNotification(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        String userId = jwt.getClaimAsString("id");
        notificationService.deleteNotification(userId, id);
        return ResponseEntity.noContent().build();
    }
}