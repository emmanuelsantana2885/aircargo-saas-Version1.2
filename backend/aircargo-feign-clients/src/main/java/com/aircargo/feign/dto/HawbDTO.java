package com.aircargo.feign.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class HawbDTO {

    private UUID id;
    private UUID mawbId;
    private UUID airlineId;
    private String hawbNumber;
    private String consigneeName;
    private String destination;
    private Integer pieces;
    private BigDecimal weightKg;
    private String commodityType;
    private String status;
    private String notes;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMawbId() { return mawbId; }
    public void setMawbId(UUID mawbId) { this.mawbId = mawbId; }
    public UUID getAirlineId() { return airlineId; }
    public void setAirlineId(UUID airlineId) { this.airlineId = airlineId; }
    public String getHawbNumber() { return hawbNumber; }
    public void setHawbNumber(String hawbNumber) { this.hawbNumber = hawbNumber; }
    public String getConsigneeName() { return consigneeName; }
    public void setConsigneeName(String consigneeName) { this.consigneeName = consigneeName; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public Integer getPieces() { return pieces; }
    public void setPieces(Integer pieces) { this.pieces = pieces; }
    public BigDecimal getWeightKg() { return weightKg; }
    public void setWeightKg(BigDecimal weightKg) { this.weightKg = weightKg; }
    public String getCommodityType() { return commodityType; }
    public void setCommodityType(String commodityType) { this.commodityType = commodityType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
