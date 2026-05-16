package com.irrigation_system.iot.service;

import com.irrigation_system.iot.dto.NotificationDTO;
import com.irrigation_system.iot.entity.Device;
import com.irrigation_system.iot.entity.Notification;
import com.irrigation_system.iot.entity.UserEntity;
import com.irrigation_system.iot.repository.NotificationRepository;
import com.irrigation_system.iot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Persists in-app notifications and pushes them over WebSocket for watering events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WateringNotificationService {

    public static final String TYPE_SCHEDULE_TRIGGERED = "schedule_triggered";
    public static final String TYPE_WATERING_DONE = "watering_done";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void notifyScheduleTriggered(Device device, float waterAmountMl) {
        String message = String.format(
                "Device \"%s\" started scheduled watering (target %.0f ml).",
                device.getName(),
                waterAmountMl
        );
        publish(device, TYPE_SCHEDULE_TRIGGERED, "Scheduled watering started", message);
    }

    @Transactional
    public void notifyWateringCompleted(Device device, Float waterAmountMl, boolean fromSchedule) {
        String title = fromSchedule ? "Scheduled watering completed" : "Watering completed";
        String amountPart = waterAmountMl != null
                ? String.format(" (~%.0f ml).", waterAmountMl)
                : ".";
        String message = String.format(
                "Device \"%s\" finished %swatering%s",
                device.getName(),
                fromSchedule ? "scheduled " : "",
                amountPart
        );
        publish(device, TYPE_WATERING_DONE, title, message);
    }

    private void publish(Device device, String type, String title, String message) {
        Optional<String> userId = resolveOwnerUserId(device);
        if (userId.isEmpty()) {
            log.warn("Skipping watering notification for device {}: no owner", device.getId());
            return;
        }

        UserEntity user = userRepository.findById(userId.get()).orElse(null);
        if (user == null) {
            log.warn("Skipping watering notification for device {}: owner user {} not found", device.getId(), userId.get());
            return;
        }

        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setOl(0L);
        notification.setCreatedAt(Instant.now());
        notification.setUser(user);
        notification.setDevice(device);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setIsRead(false);

        notification = notificationRepository.save(notification);
        NotificationDTO dto = toDto(notification);

        messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), dto);
        log.info("Published {} notification for user {} device {}", type, user.getId(), device.getId());
    }

    private Optional<String> resolveOwnerUserId(Device device) {
        if (device.getOwnerId() != null && !device.getOwnerId().isBlank()) {
            return Optional.of(device.getOwnerId());
        }
        if (device.getUser() != null && device.getUser().getId() != null) {
            return Optional.of(device.getUser().getId());
        }
        return Optional.empty();
    }

    private NotificationDTO toDto(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setDeviceId(notification.getDevice() != null ? notification.getDevice().getId() : null);
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setIsRead(notification.getIsRead());
        dto.setReadAt(notification.getReadAt());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
