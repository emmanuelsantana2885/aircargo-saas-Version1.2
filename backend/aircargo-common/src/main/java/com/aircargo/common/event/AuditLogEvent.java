package com.aircargo.common.event;

import java.util.UUID;

public record AuditLogEvent(
        UUID userId,
        String email,
        String fullName,
        String action,
        String entityType,
        String entityId,
        String details,
        String ipAddress
) {}
