package com.aircargo.dto;


import com.aircargo.common.entity.CommodityType;
import com.aircargo.entity.UldAwb;
import com.aircargo.entity.Uld;
import com.aircargo.entity.Mawb;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UldAwbDTO {

    private UUID id;
    private UUID uldId;
    private UUID mawbId;

    private String mawbLabel;
    private CommodityType description;
    private String destination;
    private Integer pieces;
    private Integer piecesPct;

    private BigDecimal tempInbound;
    private BigDecimal tempOutbound;
    private Boolean hc;
    private String comments;

    private BigDecimal consumptionPallets;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer avgTimePerPieceSec;

    //Campos calculados
    private BigDecimal lapseMinutes;
    private BigDecimal pcsPerMin;
    private BigDecimal operativeWorkedHours;
    private BigDecimal earnedHours;

    private OffsetDateTime createdAt;

    public static UldAwbDTO fromEntity(UldAwb entity){
        if(entity == null) return null;
        String mawbLabel = entity.getMawbLabel();
        if (mawbLabel == null && entity.getMawb() != null) {
            mawbLabel = entity.getMawb().getAwbNumber();
        }
        return UldAwbDTO.builder()
                .id(entity.getId())
                .uldId(entity.getUld() != null ? entity.getUld().getId() : null)
                .mawbId(entity.getMawb() != null ? entity.getMawb().getId() : null)
                .mawbLabel(mawbLabel)
                .description(entity.getDescription())
                .destination(entity.getDestination())
                .pieces(entity.getPieces())
                .piecesPct(entity.getPiecesPct())
                .tempInbound(entity.getTempInbound())
                .tempOutbound(entity.getTempOutbound())
                .hc(entity.getHc())
                .comments(entity.getComments())
                .consumptionPallets(entity.getConsumptionPallets())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .avgTimePerPieceSec(entity.getAvgTimePerPieceSec())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static UldAwb toEntity(UldAwbDTO dto){
        if(dto == null) return null;
        UldAwb entity = new UldAwb();
        entity.setId(dto.getId());
        if (dto.getUldId() != null) {
            Uld u = new Uld();
            u.setId(dto.getUldId());
            entity.setUld(u);
        }
        if (dto.getMawbId() != null) {
            Mawb m = new Mawb();
            m.setId(dto.getMawbId());
            entity.setMawb(m);
        }
        entity.setMawbLabel(dto.getMawbLabel());
        entity.setDescription(dto.getDescription());
        entity.setDestination(dto.getDestination());
        entity.setPieces(dto.getPieces());
        entity.setPiecesPct(dto.getPiecesPct());
        entity.setTempInbound(dto.getTempInbound());
        entity.setTempOutbound(dto.getTempOutbound());
        entity.setHc(dto.getHc());
        entity.setComments(dto.getComments());
        entity.setConsumptionPallets(dto.getConsumptionPallets());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setAvgTimePerPieceSec(dto.getAvgTimePerPieceSec());
        return entity;
        }
        
}
