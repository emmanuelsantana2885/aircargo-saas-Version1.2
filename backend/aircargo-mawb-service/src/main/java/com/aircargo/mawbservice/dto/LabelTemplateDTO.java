package com.aircargo.mawbservice.dto;

import com.aircargo.mawbservice.entity.LabelTemplate;
import com.aircargo.mawbservice.entity.LabelType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class LabelTemplateDTO {

    private UUID id;
    private String name;
    private LabelType type;
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

    public static LabelTemplateDTO fromEntity(LabelTemplate t) {
        if (t == null) return null;
        LabelTemplateDTO dto = new LabelTemplateDTO();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setType(t.getType());
        dto.setWidthInches(t.getWidthInches());
        dto.setHeightInches(t.getHeightInches());
        dto.setOrientation(t.getOrientation());
        dto.setDpi(t.getDpi());
        dto.setConfigJson(t.getConfigJson());
        dto.setIsDefault(t.getIsDefault());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setUpdatedAt(t.getUpdatedAt());
        return dto;
    }

    public static LabelTemplate toEntity(LabelTemplateDTO dto) {
        if (dto == null) return null;
        LabelTemplate t = new LabelTemplate();
        t.setId(dto.getId());
        t.setName(dto.getName());
        t.setType(dto.getType() != null ? dto.getType() : LabelType.CARGO);
        t.setWidthInches(dto.getWidthInches());
        t.setHeightInches(dto.getHeightInches());
        t.setOrientation(dto.getOrientation() != null ? dto.getOrientation() : "HORIZONTAL");
        t.setDpi(dto.getDpi() != null ? dto.getDpi() : 203);
        t.setConfigJson(dto.getConfigJson() != null ? dto.getConfigJson() : "{\"elements\":[]}");
        t.setIsDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false);
        return t;
    }
}
