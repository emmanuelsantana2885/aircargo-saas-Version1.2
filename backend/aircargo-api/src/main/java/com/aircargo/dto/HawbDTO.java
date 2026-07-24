package com.aircargo.dto;

import com.aircargo.common.entity.Airline;
import com.aircargo.common.entity.CommodityType;
import com.aircargo.entity.Hawb;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class HawbDTO {
    private UUID id;
    private UUID mawbId;
    private UUID airlineId;

    @NotBlank(message = "hawbNumber es requerido")
    private String hawbNumber;

    private String consigneeName;
    private String destination;

    @NotNull(message = "pieces es requerido")
    private Integer pieces;

    private BigDecimal weightKg;
    private CommodityType commodityType;
    private MawbStatus status;
    private String notes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static HawbDTO fromEntity(Hawb entity){
        if(entity == null) return null;
        return HawbDTO.builder()
                .id(entity.getId())
                .mawbId(entity.getMawb() != null ? entity.getMawb().getId() : null)
                .airlineId(entity.getAirline() != null ? entity.getAirline().getId() : null)
                .hawbNumber(entity.getHawbNumber())
                .consigneeName(entity.getConsigneeName())
                .destination(entity.getDestination())
                .pieces(entity.getPieces())
                .weightKg(entity.getWeightKg())
                .commodityType(entity.getCommodityType())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static Hawb toEntity(HawbDTO dto){
        if(dto == null) return null;
        Hawb entity = new Hawb();
        entity.setId(dto.getId());
        if (dto.getMawbId() != null) {
            Mawb mawb = new Mawb();
            mawb.setId(dto.getMawbId());
            entity.setMawb(mawb);
        }
        if (dto.getAirlineId() != null) {
            Airline airline = new Airline();
            airline.setId(dto.getAirlineId());
            entity.setAirline(airline);
        }
        entity.setHawbNumber(dto.getHawbNumber());
        entity.setConsigneeName(dto.getConsigneeName());
        entity.setDestination(dto.getDestination());
        entity.setPieces(dto.getPieces());
        entity.setWeightKg(dto.getWeightKg());
        entity.setCommodityType(dto.getCommodityType());
        entity.setStatus(dto.getStatus());
        entity.setNotes(dto.getNotes());
        return entity;
    }
}
