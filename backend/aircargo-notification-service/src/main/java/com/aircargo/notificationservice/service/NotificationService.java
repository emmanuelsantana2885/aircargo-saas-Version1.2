package com.aircargo.notificationservice.service;

import com.aircargo.notificationservice.dto.NotificationDTO;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationDTO> getNotificationsByUserId(UUID userId);

    List<NotificationDTO> getUnreadNotificationsByUserId(UUID userId);

    long getUnreadCount(UUID userId);

    NotificationDTO markAsRead(UUID notificationId);

    void createNotification(UUID userId, String type, String title, String body,
                            String entityType, UUID entityId);

    void deleteNotification(UUID notificationId);
}
