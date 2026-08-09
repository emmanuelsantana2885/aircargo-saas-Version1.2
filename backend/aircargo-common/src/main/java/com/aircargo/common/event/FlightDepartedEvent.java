package com.aircargo.common.event;

import java.util.UUID;

public record FlightDepartedEvent(
    UUID flightId,
    String flightNumber,
    UUID airlineId
) {}
