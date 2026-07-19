package com.aircargo.event.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FlightCreatedEvent(
    UUID flightId,
    String flightNumber,
    UUID airlineId,
    OffsetDateTime departureDate,
    String origin,
    String destination
) {}
