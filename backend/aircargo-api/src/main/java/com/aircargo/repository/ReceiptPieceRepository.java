package com.aircargo.repository;

import com.aircargo.entity.ReceiptPiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReceiptPieceRepository extends JpaRepository<ReceiptPiece, UUID> {

    List<ReceiptPiece> findByReceiptId(UUID id);

    @Modifying
    @Query("DELETE FROM ReceiptPiece p WHERE p.receipt.id = :receiptId")
    void deleteByReceiptId(UUID receiptId);
}