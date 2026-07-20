package com.aircargo.repository;

import com.aircargo.entity.Uld;
import com.aircargo.entity.UldAwb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UldAwbRepository extends JpaRepository<UldAwb, UUID> {

    List<UldAwb> findByUldId(UUID uldId);

    List<UldAwb> findByUldIdIn(List<UUID> uldIds);

    List<UldAwb> findByMawbId(UUID mawbId);

    List<UldAwb> findByUld(Uld uld);

    Optional<UldAwb> findByUldIdAndMawbId(UUID uldId, UUID mawbId);
}
