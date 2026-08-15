package com.aircargo.common.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LabelPrintRequest {

    private UUID templateId;
    private String format = "PDF";
    private List<UUID> ids;
    private Integer quantity = 1;
    private Map<String, Map<String, String>> overrides;

    public UUID getTemplateId() { return templateId; }
    public void setTemplateId(UUID templateId) { this.templateId = templateId; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public List<UUID> getIds() { return ids; }
    public void setIds(List<UUID> ids) { this.ids = ids; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Map<String, Map<String, String>> getOverrides() { return overrides; }
    public void setOverrides(Map<String, Map<String, String>> overrides) { this.overrides = overrides; }
}
