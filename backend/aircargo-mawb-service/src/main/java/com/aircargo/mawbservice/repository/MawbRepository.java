package com.aircargo.mawbservice.repository;

import com.aircargo.mawbservice.entity.Mawb;
import com.aircargo.mawbservice.entity.MawbStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MawbRepository extends JpaRepository<Mawb, UUID> {
    List<Mawb> findByAirlineId(UUID airlineId);
    Page<Mawb> findByAirlineId(UUID airlineId, Pageable pageable);

    List<Mawb> findByFlightId(UUID flightId);
    Page<Mawb> findByFlightId(UUID flightId, Pageable pageable);

    List<Mawb> findByAirlineIdAndStatus(UUID airlineId, MawbStatus status);
    Page<Mawb> findByAirlineIdAndStatus(UUID airlineId, MawbStatus status, Pageable pageable);

    List<Mawb> findByAirlineIdAndFlightId(UUID airlineId, UUID flightId);
    Page<Mawb> findByAirlineIdAndFlightId(UUID airlineId, UUID flightId, Pageable pageable);

    List<Mawb> findByAirlineIdAndFlightIdAndStatus(UUID airlineId, UUID flightId, MawbStatus status);
    Page<Mawb> findByAirlineIdAndFlightIdAndStatus(UUID airlineId, UUID flightId, MawbStatus status, Pageable pageable);

    Optional<Mawb> findByAwbNumber(String awbNumber);
    boolean existsByAwbNumber(String awbNumber);
}