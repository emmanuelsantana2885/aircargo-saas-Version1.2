package com.aircargo.repository;

import com.aircargo.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Page<AuditLog> findByUserId(UUID userId, Pageable pageable);
    List<AuditLog> findAllByOrderByCreatedAtDesc();
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId);
}
