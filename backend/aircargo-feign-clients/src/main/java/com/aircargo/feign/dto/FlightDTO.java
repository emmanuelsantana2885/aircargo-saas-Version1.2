package com.aircargo.feign.dto;

import java.time.LocalDate;
import java.util.UUID;

public class FlightDTO {
    private UUID id;
    private UUID airlineId;
    private String flightNumber;
    private String origin;
    private String destination;
    private String aircraftReg;
    private String aircraftType;
    private LocalDate flightDate;
    private String status;
    private Double maxPayloadKg;
    private Integer totalPositions;
    private String notes;

    public FlightDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAirlineId() { return airlineId; }
    public void setAirlineId(UUID airlineId) { this.airlineId = airlineId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public String getAircraftReg() { return aircraftReg; }
    public void setAircraftReg(String aircraftReg) { this.aircraftReg = aircraftReg; }
    public String getAircraftType() { return aircraftType; }
    public void setAircraftType(String aircraftType) { this.aircraftType = aircraftType; }
    public LocalDate getFlightDate() { return flightDate; }
    public void setFlightDate(LocalDate flightDate) { this.flightDate = flightDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getMaxPayloadKg() { return maxPayloadKg; }
    public void setMaxPayloadKg(Double maxPayloadKg) { this.maxPayloadKg = maxPayloadKg; }
    public Integer getTotalPositions() { return totalPositions; }
    public void setTotalPositions(Integer totalPositions) { this.totalPositions = totalPositions; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
