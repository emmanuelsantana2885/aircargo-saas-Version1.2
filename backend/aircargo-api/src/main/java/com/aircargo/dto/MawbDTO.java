package com.aircargo.dto;

import com.aircargo.common.entity.Airline;
import com.aircargo.common.entity.CommodityType;
import com.aircargo.entity.Flight;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MawbDTO {

    private UUID id;
    private UUID airlineId;
    private UUID flightId;

    @NotBlank(message = "awbNumber es requerido")
    private String awbNumber;

    private String shipperName;
    private String consigneeName;

    @NotNull(message = "origin es requerido")
    private String origin;

    @NotNull(message = "destination es requerido")
    private String destination;

    @NotNull(message = "pieces es requerido")
    private Integer pieces;

    private BigDecimal reportedWeightKg;
    private BigDecimal chargeableWeightKg;
    private CommodityType commodityType;
    private MawbStatus status;
    private Boolean cashOnly;
    private Boolean bookedInAcoms;
    private Boolean docsProvided;
    private Boolean customsCompleted;
    private Boolean preBuilt;
    private Boolean looseTender;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;


    public static MawbDTO fromEntity(Mawb entity){
        if(entity == null) return null;
        return MawbDTO.builder()
                .id(entity.getId())
                .airlineId(entity.getAirline() != null ? entity.getAirline().getId() : null)
                .flightId(entity.getFlight() != null ? entity.getFlight().getId() : null)
                .awbNumber(entity.getAwbNumber())
                .shipperName(entity.getShipperName())
                .consigneeName(entity.getConsigneeName())
                .origin(entity.getOrigin())
                .destination(entity.getDestination())
                .pieces(entity.getPieces())
                .reportedWeightKg(entity.getReportedWeightKg())
                .chargeableWeightKg(entity.getChargeableWeightKg())
                .commodityType(entity.getCommodityType())
                .status(entity.getStatus())
                .cashOnly(entity.getCashOnly())
                .bookedInAcoms(entity.getBookedInAcoms())
                .docsProvided(entity.getDocsProvided())
                .customsCompleted(entity.getCustomsCompleted())
                .preBuilt(entity.getPreBuilt())
                .looseTender(entity.getLooseTender())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Mawb toEntity(MawbDTO dto, Airline airline, Flight flight){
        if(dto == null) return null;
        Mawb entity = new Mawb();
        entity.setId(dto.getId());
        entity.setAirline(airline);
        entity.setFlight(flight);
        entity.setAwbNumber(dto.getAwbNumber());
        entity.setShipperName(dto.getShipperName());
        entity.setConsigneeName(dto.getConsigneeName());
        entity.setOrigin(dto.getOrigin());
        entity.setDestination(dto.getDestination());
        entity.setPieces(dto.getPieces());
        entity.setReportedWeightKg(dto.getReportedWeightKg());
        entity.setChargeableWeightKg(dto.getChargeableWeightKg());
        entity.setCommodityType(dto.getCommodityType());
        entity.setStatus(dto.getStatus());
        entity.setCashOnly(dto.getCashOnly());
        entity.setBookedInAcoms(dto.getBookedInAcoms());
        entity.setDocsProvided(dto.getDocsProvided());
        entity.setCustomsCompleted(dto.getCustomsCompleted());
        entity.setPreBuilt(dto.getPreBuilt());
        entity.setLooseTender(dto.getLooseTender());
        entity.setNotes(dto.getNotes());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }
}
