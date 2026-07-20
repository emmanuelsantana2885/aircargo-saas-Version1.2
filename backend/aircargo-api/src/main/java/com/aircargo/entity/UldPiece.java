package com.aircargo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "uld_piece")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UldPiece {

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

    @Column(name = "awb_number", length = 20)
    private String awbNumber;

    @Column(name = "hawb_number", length = 30)
    private String hawbNumber;

    @Column(name = "piece_number", nullable = false)
    private Integer pieceNumber;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "source", nullable = false, columnDefinition = "piece_source")
    private PieceSource source = PieceSource.MANUAL;

    @Column(name = "scanned_by")
    private UUID scannedBy;

    @Column(name = "scanned_at")
    private OffsetDateTime scannedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
