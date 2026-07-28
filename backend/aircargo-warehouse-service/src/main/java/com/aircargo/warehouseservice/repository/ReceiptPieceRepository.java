package com.aircargo.warehouseservice.repository;

import com.aircargo.warehouseservice.entity.ReceiptPiece;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReceiptPieceRepository extends JpaRepository<ReceiptPiece, UUID> {
    List<ReceiptPiece> findByReceiptId(UUID receiptId);
    void deleteByReceiptId(UUID receiptId);
}