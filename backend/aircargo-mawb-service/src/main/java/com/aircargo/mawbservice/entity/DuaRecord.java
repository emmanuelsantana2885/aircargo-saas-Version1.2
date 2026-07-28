package com.aircargo.mawbservice.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "dua_record")
public class DuaRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "mawb_id", nullable = false)
    private UUID mawbId;

    @Column(name = "dua_number", nullable = false, length = 50)
    private String duaNumber;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DuaStatus status = DuaStatus.PENDING;

    @Column(name = "dua_date")
    private LocalDate duaDate;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "customs_broker", length = 150)
    private String customsBroker;

    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public DuaRecord() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMawbId() { return mawbId; }
    public void setMawbId(UUID mawbId) { this.mawbId = mawbId; }
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
