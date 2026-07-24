package com.aircargo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.aircargo.common.entity.Airline;
import com.aircargo.common.entity.CommodityType;
import java.util.UUID;

@Entity
@Table(name = "mawb")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mawb {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Airline airline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Flight flight;

    @Column(name = "awb_number", nullable = false, length = 20)
    private String awbNumber;

    @Column(name = "shipper_name", length = 150)
    private String shipperName;

    @Column(name = "consignee_name", length = 150)
    private String consigneeName;

    @Column(name = "origin", nullable = false, columnDefinition = "bpchar(3)")
    private String origin;

    @Column(name = "destination", nullable = false, columnDefinition = "bpchar(3)")
    private String destination;

    @Column(name = "pieces", nullable = false)
    private Integer pieces = 1;

    @Column(name = "reported_weight_kg", precision = 10, scale = 2)
    private BigDecimal reportedWeightKg;

    @Column(name = "chargeable_weight_kg", precision = 10, scale = 2)
    private BigDecimal chargeableWeightKg;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "commodity_type", nullable = false, columnDefinition = "commodity_type")
    private CommodityType commodityType = CommodityType.DRY_CARGO;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "mawb_status")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private MawbStatus status = MawbStatus.BOOKED;

    @Column(name = "cash_only")
    private Boolean cashOnly = false;

    @Column(name = "booked_in_acoms")
    private Boolean bookedInAcoms = false;

    @Column(name = "docs_provided")
    private Boolean docsProvided = false;

    @Column(name = "customs_completed")
    private Boolean customsCompleted = false;

    @Column(name = "pre_built")
    private Boolean preBuilt = false;

    @Column(name = "loose_tender")
    private Boolean looseTender = false;

    @Column(name = "supporting_docs", columnDefinition = "text")
    private String supportingDocs = "[]";

    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private OffsetDateTime updatedAt;
}
