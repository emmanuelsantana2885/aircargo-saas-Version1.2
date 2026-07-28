package com.aircargo.exportservice.repository;

import com.aircargo.exportservice.entity.WarehouseReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WarehouseReceiptRepository extends JpaRepository<WarehouseReceiptEntity, UUID> {
    List<WarehouseReceiptEntity> findBySupersededFalse();
}
