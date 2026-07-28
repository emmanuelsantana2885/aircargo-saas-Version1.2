package com.aircargo.uldservice.repository;

import com.aircargo.uldservice.entity.UldPiece;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UldPieceRepository extends JpaRepository<UldPiece, UUID> {
    List<UldPiece> findByUldId(UUID uldId);
    List<UldPiece> findByUldIdAndMawbId(UUID uldId, UUID mawbId);
    Optional<UldPiece> findFirstByUldIdAndMawbIdOrderByPieceNumberDesc(UUID uldId, UUID mawbId);
    long countByUldIdAndMawbId(UUID uldId, UUID mawbId);
    void deleteByUldIdAndMawbId(UUID uldId, UUID mawbId);
}
