package com.aircargo.repository;

import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface MawbRepository extends JpaRepository<Mawb, UUID> {

    Optional<Mawb> findByAirlineIdAndAwbNumber(UUID airlineId, String awbNumber);

    Optional<Mawb> findByAwbNumber(String awbNumber);

    List<Mawb> findByAirlineId(UUID airlineId);

    List<Mawb> findByFlightId(UUID flightId);

    List<Mawb> findByAirlineIdAndStatus(UUID airlineId, MawbStatus status);

}
