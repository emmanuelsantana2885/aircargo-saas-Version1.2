package com.aircargo.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import com.aircargo.common.entity.CommodityType;
import java.util.UUID;

@Entity
@Table(name = "uld_awb")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UldAwb {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uld_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Uld uld;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mawb_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Mawb mawb;

    @Column(name = "mawb_label", length = 50)
    private String mawbLabel;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "description", nullable = false, columnDefinition = "commodity_type")
    private CommodityType description = CommodityType.DRY_CARGO;

    @Column(name = "destination", columnDefinition = "bpchar(3)")
    private String destination;

    @Column(name = "pieces")
    private Integer pieces = 0;

    @Column(name= "pieces_pct")
    private Integer piecesPct = 100;

    @Column(name = "temp_inbound", precision = 6, scale =2)
    private BigDecimal tempInbound;

    @Column(name = "temp_outbound", precision = 6, scale=2)
    private BigDecimal tempOutbound;

    @Column(name = "hc")
    private Boolean hc = false;

    @Column(name = "comments")
    private String comments;

    @Column(name = "consumption_pallets", precision =6 , scale =3)
    private BigDecimal consumptionPallets;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "avg_time_per_piece_sec")
    private Integer avgTimePerPieceSec = 5;

    // Campos generado de forma automatica por PostgreSQL
    @Column(name = "lapse_minutes", insertable = false, updatable = false)
    private BigDecimal lapseMinutes;

    @Column(name = "pcs_per_min", insertable = false, updatable = false)
    private BigDecimal pcsPerMin;

    @Column(name = "operative_worked_hours", insertable = false, updatable = false)
    private BigDecimal operativeWorkedHours;

    @Column(name = "earned_hours", insertable = false, updatable = false)
    private BigDecimal earnedHours;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;




}
