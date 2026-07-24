package com.aircargo.repository;

import com.aircargo.entity.Hawb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HawbRepository extends JpaRepository<Hawb, UUID> {
    
    /**
     * Encuentra todas las guías de casa pertenecientes a una guía maestra (MAWB).
     */
    List<Hawb> findByMawbId(UUID mawbId);

    Page<Hawb> findByMawbId(UUID mawbId, Pageable pageable);

    /**
     * Recupera las guías de casa asociadas a una aerolínea específica para aislamiento multi-tenant.
     */
    List<Hawb> findByAirlineId(UUID airlineId);

    /**
     * Busca un HAWB por su número (para resolución desde barcode).
     */
    Optional<Hawb> findByHawbNumber(String hawbNumber);
}
