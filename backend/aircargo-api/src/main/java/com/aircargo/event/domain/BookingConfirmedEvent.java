package com.aircargo.event.domain;

import java.util.UUID;

public record BookingConfirmedEvent(
    UUID bookingId,
    UUID mawbId,
    String awbNumber,
    UUID flightId
) {}
