package com.aircargo.dto;

import com.aircargo.common.entity.Airline;
import com.aircargo.entity.AppUser;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.WarehouseReceipt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseReceiptDTO {
    private UUID id;
    private UUID airlineId;
    private UUID mawbId;
    private UUID createdByUserId;

    private String gatewayCfs;
    private String shipperName;
    private String consigneeName;
    private String agentName;
    private String origin;
    private String destination;

    private Integer awbReportedPieces;
    private BigDecimal mawbWeightGreatest;
    private BigDecimal shipperReportedWeight;

    private OffsetDateTime startDatetime;
    private OffsetDateTime receiptDate;

    private Boolean cashOnly;
    private Boolean bookedInAcoms;
    private Boolean docsProvided;
    private Boolean customsCompleted;
    private Boolean preBuilt;
    private Boolean looseTender;

    private Integer pieceCount;
    private BigDecimal dimFactorDom;
    private BigDecimal dimFactorIntl;

    private BigDecimal actualWeightLbs;
    private BigDecimal actualWeightKg;
    private BigDecimal chargeableWeightLbs;
    private BigDecimal chargeableWeightKg;

    private String shipperComment;
    private String observations;
    private String remarks;
    private String createdByName;
    private String deliveredByName;
    private String deliveredByIdNum;
    private String deliveredByDocUrl;
    private String deliveredBySigUrl;
    private String receivedByName;
    private String receivedByIdNum;
    private String receivedByDocUrl;
    private String receivedBySigUrl;
    private String brokerName;
    private String brokerIdNum;
    private String brokerDocUrl;
    private String brokerSigUrl;
    private String receiptDocUrl;
    private String dockSignature;
    private String printName;

    private UUID hawbId;
    private String supportingDocs;

    private UUID correctionOfId;
    private Integer correctionNumber;
    private Boolean superseded;

    private List<ReceiptPieceDTO> pieces;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static WarehouseReceiptDTO fromEntity(WarehouseReceipt entity){
        if(entity == null) return null;
        return WarehouseReceiptDTO.builder()
                .id(entity.getId())
                .airlineId(entity.getAirline() != null ? entity.getAirline().getId() : null)
                .mawbId(entity.getMawb() != null ? entity.getMawb().getId() : null)
                .createdByUserId(entity.getCreatedByUser() != null ? entity.getCreatedByUser().getId() : null)
                .gatewayCfs(entity.getGatewayCfs())
                .shipperName(entity.getShipperName())
                .consigneeName(entity.getConsigneeName())
                .agentName(entity.getAgentName())
                .origin(entity.getOrigin())
                .destination(entity.getDestination())
                .awbReportedPieces(entity.getAwbReportedPieces())
                .mawbWeightGreatest(entity.getMawbWeightGreatest())
                .shipperReportedWeight(entity.getShipperReportedWeight())
                .startDatetime(entity.getStartDatetime())
                .receiptDate(entity.getReceiptDate())
                .cashOnly(entity.getCashOnly())
                .bookedInAcoms(entity.getBookedInAcoms())
                .docsProvided(entity.getDocsProvided())
                .customsCompleted(entity.getCustomsCompleted())
                .preBuilt(entity.getPreBuilt())
                .looseTender(entity.getLooseTender())
                .pieceCount(entity.getPieceCount())
                .dimFactorDom(entity.getDimFactorDom())
                .dimFactorIntl(entity.getDimFactorIntl())
                .actualWeightLbs(entity.getActualWeightLbs())
                .actualWeightKg(entity.getActualWeightKg())
                .chargeableWeightLbs(entity.getChargeableWeightLbs())
                .chargeableWeightKg(entity.getChargeableWeightKg())
                .shipperComment(entity.getShipperComment())
                .observations(entity.getObservations())
                .remarks(entity.getRemarks())
                .createdByName(entity.getCreatedByName())
                .deliveredByName(entity.getDeliveredByName())
                .deliveredByIdNum(entity.getDeliveredByIdNum())
                .deliveredByDocUrl(entity.getDeliveredByIdDocUrl())
                .deliveredBySigUrl(entity.getDeliveredBySigUrl())
                .receivedByName(entity.getReceivedByName())
                .receivedByIdNum(entity.getReceivedByIdNum())
                .receivedByDocUrl(entity.getReceivedByIdDocUrl())
                .receivedBySigUrl(entity.getReceivedBySigUrl())
                .brokerName(entity.getBrokerName())
                .brokerIdNum(entity.getBrokerIdNum())
                .brokerDocUrl(entity.getBrokerIdDocUrl())
                .brokerSigUrl(entity.getBrokerSigUrl())
                .receiptDocUrl(entity.getReceiptDocUrl())
                .dockSignature(entity.getDockSignature())
                .printName(entity.getPrintName())
                .hawbId(entity.getHawbId())
                .supportingDocs(entity.getSupportingDocs())
                .correctionOfId(entity.getCorrectionOfId())
                .correctionNumber(entity.getCorrectionNumber())
                .superseded(entity.getSuperseded())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();   
            }

    public static WarehouseReceipt toEntity(WarehouseReceiptDTO dto){
        if(dto == null) return null;
        WarehouseReceipt entity = new WarehouseReceipt();
        entity.setId(dto.getId());
        if (dto.getAirlineId() != null) {
            Airline airline = new Airline();
            airline.setId(dto.getAirlineId());
            entity.setAirline(airline);
        }
        if (dto.getMawbId() != null) {
            Mawb mawb = new Mawb();
            mawb.setId(dto.getMawbId());
            entity.setMawb(mawb);
        }
        if (dto.getCreatedByUserId() != null) {
            AppUser user = new AppUser();
            user.setId(dto.getCreatedByUserId());
            entity.setCreatedByUser(user);
        }
        entity.setGatewayCfs(dto.getGatewayCfs());
        entity.setShipperName(dto.getShipperName());
        entity.setConsigneeName(dto.getConsigneeName());
        entity.setAgentName(dto.getAgentName());
        entity.setOrigin(dto.getOrigin());
        entity.setDestination(dto.getDestination());
        entity.setAwbReportedPieces(dto.getAwbReportedPieces());
        entity.setMawbWeightGreatest(dto.getMawbWeightGreatest());
        entity.setShipperReportedWeight(dto.getShipperReportedWeight());
        entity.setStartDatetime(dto.getStartDatetime());
        entity.setReceiptDate(dto.getReceiptDate());
        entity.setCashOnly(dto.getCashOnly());
        entity.setBookedInAcoms(dto.getBookedInAcoms());
        entity.setDocsProvided(dto.getDocsProvided());
        entity.setCustomsCompleted(dto.getCustomsCompleted());
        entity.setPreBuilt(dto.getPreBuilt());
        entity.setLooseTender(dto.getLooseTender());
        entity.setPieceCount(dto.getPieceCount());
        entity.setDimFactorDom(dto.getDimFactorDom());
        entity.setDimFactorIntl(dto.getDimFactorIntl());
        entity.setActualWeightLbs(dto.getActualWeightLbs());
        entity.setActualWeightKg(dto.getActualWeightKg());
        entity.setChargeableWeightLbs(dto.getChargeableWeightLbs());
        entity.setChargeableWeightKg(dto.getChargeableWeightKg());
        entity.setShipperComment(dto.getShipperComment());
        entity.setObservations(dto.getObservations());
        entity.setRemarks(dto.getRemarks());
        entity.setCreatedByName(dto.getCreatedByName());
        entity.setDeliveredByName(dto.getDeliveredByName());
        entity.setDeliveredByIdNum(dto.getDeliveredByIdNum());
        entity.setDeliveredByIdDocUrl(dto.getDeliveredByDocUrl());
        entity.setDeliveredBySigUrl(dto.getDeliveredBySigUrl());
        entity.setReceivedByName(dto.getReceivedByName());
        entity.setReceivedByIdNum(dto.getReceivedByIdNum());
        entity.setReceivedByIdDocUrl(dto.getReceivedByDocUrl());
        entity.setReceivedBySigUrl(dto.getReceivedBySigUrl());
        entity.setBrokerName(dto.getBrokerName());
        entity.setBrokerIdNum(dto.getBrokerIdNum());
        entity.setBrokerIdDocUrl(dto.getBrokerDocUrl());
        entity.setBrokerSigUrl(dto.getBrokerSigUrl());
        entity.setReceiptDocUrl(dto.getReceiptDocUrl());
        entity.setDockSignature(dto.getDockSignature());
        entity.setPrintName(dto.getPrintName());
        entity.setHawbId(dto.getHawbId());
        entity.setSupportingDocs(dto.getSupportingDocs());
        entity.setCorrectionOfId(dto.getCorrectionOfId());
        entity.setCorrectionNumber(dto.getCorrectionNumber());
        entity.setSuperseded(dto.getSuperseded());
        return entity;
    }
}

