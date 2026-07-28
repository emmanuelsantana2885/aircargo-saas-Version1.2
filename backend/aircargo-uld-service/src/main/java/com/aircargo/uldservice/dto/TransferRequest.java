package com.aircargo.uldservice.dto;

import java.util.UUID;

public class TransferRequest {
    private UUID destinationFlightId;
    private String reason;

    public TransferRequest() {}

    public UUID getDestinationFlightId() { return destinationFlightId; }
    public void setDestinationFlightId(UUID destinationFlightId) { this.destinationFlightId = destinationFlightId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
