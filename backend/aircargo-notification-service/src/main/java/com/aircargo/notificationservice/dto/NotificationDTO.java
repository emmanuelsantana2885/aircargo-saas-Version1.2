package com.aircargo.notificationservice.dto;

import com.aircargo.notificationservice.entity.Notification;
import com.aircargo.notificationservice.entity.NotificationType;
import java.time.OffsetDateTime;
import java.util.UUID;

public class NotificationDTO {

    private UUID id;
    private UUID userId;
    private NotificationType type;
    private String title;
    private String body;
    private String entityType;
    private UUID entityId;
    private boolean isRead;
    private OffsetDateTime createdAt;

    public static NotificationDTO fromEntity(Notification entity) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setType(entity.getType());
        dto.setTitle(entity.getTitle());
        dto.setBody(entity.getBody());
        dto.setEntityType(entity.getEntityType());
        dto.setEntityId(entity.getEntityId());
        dto.setRead(entity.isRead());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
