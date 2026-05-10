package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.NotificationDTO;
import com.irrigation_system.iot.dto.UnreadCountDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Page<NotificationDTO> getNotifications(String userId, Pageable pageable);

    UnreadCountDTO getUnreadCount(String userId);

    NotificationDTO markAsRead(String userId, String notificationId);

    void markAllAsRead(String userId);

    void deleteNotification(String userId, String notificationId);
}