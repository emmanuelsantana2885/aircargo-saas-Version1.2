package com.aircargo.mawbservice.dto;

import com.aircargo.mawbservice.entity.Hawb;
import com.aircargo.mawbservice.entity.MawbStatus;
import com.aircargo.common.entity.CommodityType;

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
    private CommodityType commodityType;
    private MawbStatus status;
    private String notes;

    public static HawbDTO fromEntity(Hawb hawb) {
        if (hawb == null) return null;
        HawbDTO dto = new HawbDTO();
        dto.setId(hawb.getId());
        dto.setMawbId(hawb.getMawbId());
        dto.setAirlineId(hawb.getAirlineId());
        dto.setHawbNumber(hawb.getHawbNumber());
        dto.setConsigneeName(hawb.getConsigneeName());
        dto.setDestination(hawb.getDestination());
        dto.setPieces(hawb.getPieces());
        dto.setWeightKg(hawb.getWeightKg());
        dto.setCommodityType(hawb.getCommodityType());
        dto.setStatus(hawb.getStatus());
        dto.setNotes(hawb.getNotes());
        return dto;
    }

    public static Hawb toEntity(HawbDTO dto) {
        if (dto == null) return null;
        Hawb entity = new Hawb();
        entity.setId(dto.getId());
        entity.setMawbId(dto.getMawbId());
        entity.setAirlineId(dto.getAirlineId());
        entity.setHawbNumber(dto.getHawbNumber());
        entity.setConsigneeName(dto.getConsigneeName());
        entity.setDestination(dto.getDestination());
        entity.setPieces(dto.getPieces());
        entity.setWeightKg(dto.getWeightKg());
        entity.setCommodityType(dto.getCommodityType());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : MawbStatus.BOOKED);
        entity.setNotes(dto.getNotes());
        return entity;
    }

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
    public CommodityType getCommodityType() { return commodityType; }
    public void setCommodityType(CommodityType commodityType) { this.commodityType = commodityType; }
    public MawbStatus getStatus() { return status; }
    public void setStatus(MawbStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}