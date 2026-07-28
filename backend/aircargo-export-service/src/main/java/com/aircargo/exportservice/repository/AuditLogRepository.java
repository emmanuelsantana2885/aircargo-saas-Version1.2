package com.aircargo.exportservice.repository;

import com.aircargo.exportservice.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, String> {
    List<AuditLogEntity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);
}
