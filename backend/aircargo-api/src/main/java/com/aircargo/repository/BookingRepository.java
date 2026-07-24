package com.aircargo.repository;

import com.aircargo.common.entity.CommodityType;
import com.aircargo.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByFlightId(UUID flightId);
    Page<Booking> findByFlightId(UUID flightId, Pageable pageable);

    List<Booking> findByAirlineId(UUID airlineId);
    Page<Booking> findByAirlineId(UUID airlineId, Pageable pageable);

    List<Booking> findByMawbId(UUID mawbId);
    List<Booking> findByFlightIdAndCommodityType(
            UUID flightId,
            CommodityType commodityType);

}
