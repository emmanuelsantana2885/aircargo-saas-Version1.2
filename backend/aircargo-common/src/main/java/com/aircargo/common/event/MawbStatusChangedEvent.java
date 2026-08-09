package com.aircargo.common.event;

import java.util.UUID;

public record MawbStatusChangedEvent(
    UUID mawbId,
    String awbNumber,
    String oldStatus,
    String newStatus
) {}
