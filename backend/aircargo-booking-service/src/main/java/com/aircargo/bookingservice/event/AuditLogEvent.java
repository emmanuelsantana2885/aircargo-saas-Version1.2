package com.aircargo.bookingservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEvent {
    private UUID userId;
    private String email;
    private String fullName;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private String ipAddress;
}