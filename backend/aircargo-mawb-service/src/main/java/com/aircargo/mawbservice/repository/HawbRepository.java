package com.aircargo.mawbservice.repository;

import com.aircargo.mawbservice.entity.Hawb;
import com.aircargo.mawbservice.entity.Mawb;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HawbRepository extends JpaRepository<Hawb, UUID> {
    List<Hawb> findByMawbId(UUID mawbId);
    Page<Hawb> findByMawbId(UUID mawbId, Pageable pageable);

    List<Hawb> findByAirlineId(UUID airlineId);
    Page<Hawb> findByAirlineId(UUID airlineId, Pageable pageable);

    Hawb findByHawbNumber(String hawbNumber);
    boolean existsByHawbNumber(String hawbNumber);
}