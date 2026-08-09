package com.aircargo.uldservice.repository;

import com.aircargo.uldservice.entity.UldTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UldTypeConfigRepository extends JpaRepository<UldTypeConfig, UUID> {
    List<UldTypeConfig> findByAirlineId(UUID airlineId);

    void deleteByAirlineId(UUID airlineId);
}
