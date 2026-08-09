package com.aircargo.common.event;

import java.util.UUID;

public record BookingAwbUpdatedEvent(
    UUID bookingId,
    String awbNumber,
    UUID flightId
) {}
