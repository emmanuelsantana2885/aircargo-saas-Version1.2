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
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.aircargo.common.entity.Airline;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "uld")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Uld {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Airline airline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Flight flight;

    @Column(name = "uld_number", nullable = false, length = 30)
    private String uldNumber;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "uld_type", nullable = false, columnDefinition = "uld_type")
    private UldType uldType;

    @Column(name = "position", length = 10)
    private String position;

    @Column(name = "config", length = 10)
    private String config;

    @Column(name = "seal_number", length = 50)
    private String sealNumber;

    @Column(name = "tare_lbs", nullable = false, precision = 8, scale = 2)
    private BigDecimal tareLbs = BigDecimal.ZERO;

    @Column(name = "tare_notes", length = 200)
    private String tareNotes;

    @Column(name = "gross_weight_lbs", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossWeightLbs = BigDecimal.ZERO;

    @Column(name = "net_weight_lbs", insertable = false, updatable = false)
    private BigDecimal netWeightLbs;

    @Column(name = "tare_kg")
    private BigDecimal tareKg;

    @Column(name = "gross_weight_kg")
    private BigDecimal grossWeightKg;

    @Column(name = "net_weight_kg")
    private BigDecimal netWeightKg;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "uld_status")
    private UldStatus status = UldStatus.OPEN;

    @Column(name = "built_at")
    private OffsetDateTime builtAt;

    @Column(name = "loaded_at")
    private OffsetDateTime loadedAt;

    @Column(name = "notes")
    private String notes;

    @OneToMany(mappedBy = "uld", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<UldPiece> pieces = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
