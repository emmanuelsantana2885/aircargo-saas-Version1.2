package com.aircargo.feign.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class MawbDTO {
    private UUID id;
    private UUID airlineId;
    private UUID flightId;
    private String awbNumber;
    private String shipperName;
    private String consigneeName;
    private String origin;
    private String destination;
    private Integer pieces;
    private BigDecimal reportedWeightKg;
    private BigDecimal chargeableWeightKg;
    private String commodityType;
    private String status;
    private Boolean cashOnly;
    private Boolean bookedInAcoms;
    private Boolean docsProvided;
    private Boolean customsCompleted;
    private Boolean preBuilt;
    private String notes;

    public MawbDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAirlineId() { return airlineId; }
    public void setAirlineId(UUID airlineId) { this.airlineId = airlineId; }
    public UUID getFlightId() { return flightId; }
    public void setFlightId(UUID flightId) { this.flightId = flightId; }
    public String getAwbNumber() { return awbNumber; }
    public void setAwbNumber(String awbNumber) { this.awbNumber = awbNumber; }
    public String getShipperName() { return shipperName; }
    public void setShipperName(String shipperName) { this.shipperName = shipperName; }
    public String getConsigneeName() { return consigneeName; }
    public void setConsigneeName(String consigneeName) { this.consigneeName = consigneeName; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Integer getPieces() { return pieces; }
    public void setPieces(Integer pieces) { this.pieces = pieces; }
    public BigDecimal getReportedWeightKg() { return reportedWeightKg; }
    public void setReportedWeightKg(BigDecimal reportedWeightKg) { this.reportedWeightKg = reportedWeightKg; }
    public BigDecimal getChargeableWeightKg() { return chargeableWeightKg; }
    public void setChargeableWeightKg(BigDecimal chargeableWeightKg) { this.chargeableWeightKg = chargeableWeightKg; }
    public String getCommodityType() { return commodityType; }
    public void setCommodityType(String commodityType) { this.commodityType = commodityType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getCashOnly() { return cashOnly; }
    public void setCashOnly(Boolean cashOnly) { this.cashOnly = cashOnly; }
    public Boolean getBookedInAcoms() { return bookedInAcoms; }
    public void setBookedInAcoms(Boolean bookedInAcoms) { this.bookedInAcoms = bookedInAcoms; }
    public Boolean getDocsProvided() { return docsProvided; }
    public void setDocsProvided(Boolean docsProvided) { this.docsProvided = docsProvided; }
    public Boolean getCustomsCompleted() { return customsCompleted; }
    public void setCustomsCompleted(Boolean customsCompleted) { this.customsCompleted = customsCompleted; }
    public Boolean getPreBuilt() { return preBuilt; }
    public void setPreBuilt(Boolean preBuilt) { this.preBuilt = preBuilt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
