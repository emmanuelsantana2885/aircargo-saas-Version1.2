package com.aircargo.warehouseservice.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "receipt_piece")
public class ReceiptPiece {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "receipt_id", nullable = false)
    private UUID receiptId;

    @Column(name = "hawb_id")
    private UUID hawbId;

    @Column(name = "piece_number")
    private Integer pieceNumber;

    @Column(name = "pieces")
    private Integer pieces;

    @Column(name = "length_in", precision = 10, scale = 2)
    private BigDecimal lengthIn;

    @Column(name = "width_in", precision = 10, scale = 2)
    private BigDecimal widthIn;

    @Column(name = "height_in", precision = 10, scale = 2)
    private BigDecimal heightIn;

    @Column(name = "scale_weight_lbs", precision = 10, scale = 2)
    private BigDecimal scaleWeightLbs;

    @Column(name = "scale_weight_kg", precision = 10, scale = 3)
    private BigDecimal scaleWeightKg;

    @Column(name = "dim_weight_lbs", precision = 10, scale = 2)
    private BigDecimal dimWeightLbs;

    @Column(name = "dim_weight_kg", precision = 10, scale = 3)
    private BigDecimal dimWeightKg;

    @Column(name = "chargeable_lbs", precision = 10, scale = 2)
    private BigDecimal chargeableLbs;

    @Column(name = "chargeable_kg", precision = 10, scale = 3)
    private BigDecimal chargeableKg;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getReceiptId() { return receiptId; }
    public void setReceiptId(UUID receiptId) { this.receiptId = receiptId; }
    public UUID getHawbId() { return hawbId; }
    public void setHawbId(UUID hawbId) { this.hawbId = hawbId; }
    public Integer getPieceNumber() { return pieceNumber; }
    public void setPieceNumber(Integer pieceNumber) { this.pieceNumber = pieceNumber; }
    public Integer getPieces() { return pieces; }
    public void setPieces(Integer pieces) { this.pieces = pieces; }
    public BigDecimal getLengthIn() { return lengthIn; }
    public void setLengthIn(BigDecimal lengthIn) { this.lengthIn = lengthIn; }
    public BigDecimal getWidthIn() { return widthIn; }
    public void setWidthIn(BigDecimal widthIn) { this.widthIn = widthIn; }
    public BigDecimal getHeightIn() { return heightIn; }
    public void setHeightIn(BigDecimal heightIn) { this.heightIn = heightIn; }
    public BigDecimal getScaleWeightLbs() { return scaleWeightLbs; }
    public void setScaleWeightLbs(BigDecimal scaleWeightLbs) { this.scaleWeightLbs = scaleWeightLbs; }
    public BigDecimal getScaleWeightKg() { return scaleWeightKg; }
    public void setScaleWeightKg(BigDecimal scaleWeightKg) { this.scaleWeightKg = scaleWeightKg; }
    public BigDecimal getDimWeightLbs() { return dimWeightLbs; }
    public void setDimWeightLbs(BigDecimal dimWeightLbs) { this.dimWeightLbs = dimWeightLbs; }
    public BigDecimal getDimWeightKg() { return dimWeightKg; }
    public void setDimWeightKg(BigDecimal dimWeightKg) { this.dimWeightKg = dimWeightKg; }
    public BigDecimal getChargeableLbs() { return chargeableLbs; }
    public void setChargeableLbs(BigDecimal chargeableLbs) { this.chargeableLbs = chargeableLbs; }
    public BigDecimal getChargeableKg() { return chargeableKg; }
    public void setChargeableKg(BigDecimal chargeableKg) { this.chargeableKg = chargeableKg; }
}