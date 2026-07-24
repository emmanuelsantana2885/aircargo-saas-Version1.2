package com.aircargo.repository;

import com.aircargo.entity.Uld;
import com.aircargo.entity.UldStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UldRepository extends JpaRepository<Uld, UUID> {

    List<Uld> findByFlightId(UUID flightId);

    Page<Uld> findByFlightId(UUID flightId, Pageable pageable);

    List<Uld> findByAirlineId(UUID airlineId);

    Page<Uld> findByAirlineId(UUID airlineId, Pageable pageable);

    List<Uld> findByFlightIdAndStatus(UUID flightId, UldStatus status);

    Optional<Uld> findByFlightIdAndUldNumber(UUID flightId, String uldNumber);
}
