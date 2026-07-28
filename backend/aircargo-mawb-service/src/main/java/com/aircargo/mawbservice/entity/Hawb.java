package com.aircargo.mawbservice.entity;

import com.aircargo.common.entity.CommodityType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "hawb")
public class Hawb {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "mawb_id", nullable = false)
    private UUID mawbId;

    @Column(name = "airline_id", nullable = false)
    private UUID airlineId;

    @Column(name = "hawb_number", nullable = false, length = 50)
    private String hawbNumber;

    @Column(name = "consignee_name", length = 200)
    private String consigneeName;

    @Column(name = "destination", length = 3)
    private String destination;

    @Column(name = "pieces")
    private Integer pieces;

    @Column(name = "weight_kg", precision = 10, scale = 3)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "commodity_type", length = 30)
    private CommodityType commodityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MawbStatus status;

    @Column(name = "notes", columnDefinition = "TEXT")
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
    public CommodityType getCommodityType() { return commodityType; }
    public void setCommodityType(CommodityType commodityType) { this.commodityType = commodityType; }
    public MawbStatus getStatus() { return status; }
    public void setStatus(MawbStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}