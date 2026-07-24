package com.aircargo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
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
@Table(name = "hawb")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Hawb {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mawb_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Mawb mawb;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Airline airline;

    @Column(name = "hawb_number", nullable = false, length = 30)
    private String hawbNumber;

    @Column(name = "consignee_name", length = 150)
    private String consigneeName;

    @Column(name = "destination", columnDefinition = "bpchar(3)")
    private String destination;

    @Column(name = "pieces", nullable = false)
    private Integer pieces = 1;

    @Column(name = "weight_kg", precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "commodity_type", columnDefinition = "commodity_type")
    private CommodityType commodityType = CommodityType.DRY_CARGO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "mawb_status")
    private MawbStatus status = MawbStatus.BOOKED;

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