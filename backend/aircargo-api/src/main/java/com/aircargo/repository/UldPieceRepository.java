package com.aircargo.repository;

import com.aircargo.entity.UldPiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UldPieceRepository extends JpaRepository<UldPiece, UUID> {

    List<UldPiece> findByUldId(UUID uldId);

    List<UldPiece> findByUldIdAndMawbId(UUID uldId, UUID mawbId);

    long countByUldIdAndMawbId(UUID uldId, UUID mawbId);

    long countByMawbId(UUID mawbId);

    Optional<UldPiece> findFirstByUldIdAndMawbIdOrderByPieceNumberDesc(UUID uldId, UUID mawbId);

    @Modifying
    @Query("DELETE FROM UldPiece p WHERE p.uld.id = :uldId AND p.mawb.id = :mawbId")
    void deleteByUldIdAndMawbId(UUID uldId, UUID mawbId);

    @Modifying
    @Query("DELETE FROM UldPiece p WHERE p.uld.id = :uldId")
    void deleteAllByUldId(UUID uldId);
}
