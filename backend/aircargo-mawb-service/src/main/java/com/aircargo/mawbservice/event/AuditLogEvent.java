package com.aircargo.mawbservice.event;

import java.util.UUID;

public class AuditLogEvent {
    private UUID userId;
    private String email;
    private String fullName;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private String ipAddress;

    public AuditLogEvent() {}

    public AuditLogEvent(UUID userId, String email, String fullName, String action,
                         String entityType, String entityId, String details, String ipAddress) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.ipAddress = ipAddress;
    }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}