package com.aircargo.mawbservice.dto;

import com.aircargo.mawbservice.entity.DuaRecord;
import com.aircargo.mawbservice.entity.DuaStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class DuaRecordDTO {

    private UUID id;
    private UUID mawbId;
    private String awbNumber;
    private String duaNumber;
    private String documentUrl;
    private DuaStatus status;
    private LocalDate duaDate;
    private String notes;
    private String customsBroker;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public DuaRecordDTO() {}

    public static DuaRecordDTO fromEntity(DuaRecord entity) {
        if (entity == null) return null;
        DuaRecordDTO dto = new DuaRecordDTO();
        dto.setId(entity.getId());
        dto.setMawbId(entity.getMawbId());
        dto.setDuaNumber(entity.getDuaNumber());
        dto.setDocumentUrl(entity.getDocumentUrl());
        dto.setStatus(entity.getStatus());
        dto.setDuaDate(entity.getDuaDate());
        dto.setNotes(entity.getNotes());
        dto.setCustomsBroker(entity.getCustomsBroker());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMawbId() { return mawbId; }
    public void setMawbId(UUID mawbId) { this.mawbId = mawbId; }
    public String getAwbNumber() { return awbNumber; }
    public void setAwbNumber(String awbNumber) { this.awbNumber = awbNumber; }
    public String getDuaNumber() { return duaNumber; }
    public void setDuaNumber(String duaNumber) { this.duaNumber = duaNumber; }
    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
    public DuaStatus getStatus() { return status; }
    public void setStatus(DuaStatus status) { this.status = status; }
    public LocalDate getDuaDate() { return duaDate; }
    public void setDuaDate(LocalDate duaDate) { this.duaDate = duaDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCustomsBroker() { return customsBroker; }
    public void setCustomsBroker(String customsBroker) { this.customsBroker = customsBroker; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
