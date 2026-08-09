package com.aircargo.feign.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class LabelTemplateDTO {

    private UUID id;
    private String name;
    private String type;
    private BigDecimal widthInches;
    private BigDecimal heightInches;
    private String orientation;
    private Integer dpi;
    private String configJson;
    private Boolean isDefault;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
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
