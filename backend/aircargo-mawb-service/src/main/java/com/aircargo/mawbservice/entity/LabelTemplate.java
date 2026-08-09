package com.aircargo.mawbservice.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "label_template")
public class LabelTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private LabelType type;

    @Column(name = "width_inches", nullable = false, precision = 5, scale = 2)
    private BigDecimal widthInches;

    @Column(name = "height_inches", nullable = false, precision = 5, scale = 2)
    private BigDecimal heightInches;

    @Column(name = "orientation", nullable = false, length = 20)
    private String orientation = "HORIZONTAL";

    @Column(name = "dpi", nullable = false)
    private Integer dpi = 203;

    @Column(name = "config_json", nullable = false, columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LabelType getType() { return type; }
    public void setType(LabelType type) { this.type = type; }
    public BigDecimal getWidthInches() { return widthInches; }
    public void setWidthInches(BigDecimal widthInches) { this.widthInches = widthInches; }
    public BigDecimal getHeightInches() { return heightInches; }
    public void setHeightInches(BigDecimal heightInches) { this.heightInches = heightInches; }
    public String getOrientation() { return orientation; }
    public void setOrientation(String orientation) { this.orientation = orientation; }
    public Integer getDpi() { return dpi; }
    public void setDpi(Integer dpi) { this.dpi = dpi; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
