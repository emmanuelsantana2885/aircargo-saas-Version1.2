package com.aircargo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import com.aircargo.common.entity.Airline;
import java.util.UUID;

@Entity
@Table(name = "warehouse_receipt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Airline airline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mawb_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Mawb mawb;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private AppUser createdByUser;

    @Builder.Default
    @Column(name = "gateway_cfs", length = 10)
    private String gatewayCfs = "SDQ";

    @Column(name = "shipper_name", length = 150)
    private String shipperName;

    @Column(name = "consignee_name", length = 150)
    private String consigneeName;

    @Column(name = "agent_name", length = 150)
    private String agentName;

    @Column(name = "origin", columnDefinition = "bpchar(3)")
    private String origin;

    @Column(name = "destination", columnDefinition = "bpchar(3)")
    private String destination;

    @Column(name = "awb_reported_pieces")
    private Integer awbReportedPieces;

    @Column(name = "mawb_weight_greatest", precision = 10, scale = 3)
    private BigDecimal mawbWeightGreatest;

    @Column(name = "shipper_reported_weight", precision = 10, scale = 3)
    private BigDecimal shipperReportedWeight;

    @Column(name = "start_datetime")
    private OffsetDateTime startDatetime;

    @Column(name = "receipt_date")
    private OffsetDateTime receiptDate;

    @Builder.Default
    @Column(name = "cash_only")
    private Boolean cashOnly = false;

    @Builder.Default
    @Column(name = "booked_in_acoms")
    private Boolean bookedInAcoms = false;

    @Builder.Default
    @Column(name = "docs_provided")
    private Boolean docsProvided = false;

    @Builder.Default
    @Column(name = "customs_completed")
    private Boolean customsCompleted = false;

    @Builder.Default
    @Column(name = "pre_built")
    private Boolean preBuilt = false;

    @Builder.Default
    @Column(name = "loose_tender")
    private Boolean looseTender = false;

    @Builder.Default
    @Column(name = "piece_count")
    private Integer pieceCount = 0;

    @Builder.Default
    @Column(name = "dim_factor_dom", precision = 6, scale = 0)
    private BigDecimal dimFactorDom = new BigDecimal("194");

    @Builder.Default
    @Column(name = "dim_factor_intl", precision = 6, scale = 0)
    private BigDecimal dimFactorIntl = new BigDecimal("366");

    @Builder.Default
    @Column(name = "actual_weight_lbs", precision = 10, scale = 3)
    private BigDecimal actualWeightLbs = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "actual_weight_kg", precision = 10, scale = 3)
    private BigDecimal actualWeightKg = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "chargeable_weight_lbs", precision = 10, scale = 3)
    private BigDecimal chargeableWeightLbs = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "chargeable_weight_kg", precision = 10, scale = 3)
    private BigDecimal chargeableWeightKg = BigDecimal.ZERO;

    @Column(name = "shipper_comment")
    private String shipperComment;

    @Column(name = "observations")
    private String observations;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "created_by_name", length = 150)
    private String createdByName;

    @Column(name = "delivered_by_name", length = 150)
    private String deliveredByName;

    @Column(name = "delivered_by_id_num", length = 50)
    private String deliveredByIdNum;

    @Column(name = "delivered_by_id_doc_url")
    private String deliveredByIdDocUrl;

    @Column(name = "delivered_by_sig_url", columnDefinition = "text")
    private String deliveredBySigUrl;

    @Column(name = "received_by_name", length = 150)
    private String receivedByName;

    @Column(name = "received_by_id_num", length = 50)
    private String receivedByIdNum;

    @Column(name = "received_by_id_doc_url")
    private String receivedByIdDocUrl;

    @Column(name = "received_by_sig_url", columnDefinition = "text")
    private String receivedBySigUrl;

    @Column(name = "broker_name", length = 150)
    private String brokerName;

    @Column(name = "broker_id_num", length = 50)
    private String brokerIdNum;

    @Column(name = "broker_id_doc_url")
    private String brokerIdDocUrl;

    @Column(name = "broker_sig_url", columnDefinition = "text")
    private String brokerSigUrl;

    @Column(name = "receipt_doc_url")
    private String receiptDocUrl;

    @Column(name = "dock_signature", columnDefinition = "text")
    private String dockSignature;

    @Builder.Default
    @Column(name = "supporting_docs", columnDefinition = "text")
    private String supportingDocs = "[]";

    @Column(name = "hawb_id")
    private UUID hawbId;

    @Column(name = "print_name", length = 150)
    private String printName;

    @Column(name = "excel_data", columnDefinition = "bytea")
    @JsonIgnore
    private byte[] excelData;

    @Column(name = "pdf_data", columnDefinition = "bytea")
    @JsonIgnore
    private byte[] pdfData;

    @OneToMany(mappedBy = "receipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("receipt")
    @Builder.Default
    private List<ReceiptPiece> pieces = new ArrayList<>();

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty(access = com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY)
    private List<String> bookingCorrections = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
