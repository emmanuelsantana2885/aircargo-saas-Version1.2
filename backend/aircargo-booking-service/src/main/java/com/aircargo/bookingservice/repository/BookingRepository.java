package com.aircargo.bookingservice.repository;

import com.aircargo.bookingservice.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByMawbId(UUID mawbId);
    List<Booking> findByFlightId(UUID flightId);
    List<Booking> findByAirlineId(UUID airlineId);
    Page<Booking> findByFlightId(UUID flightId, Pageable pageable);
    Page<Booking> findByAirlineId(UUID airlineId, Pageable pageable);
    Page<Booking> findAll(Pageable pageable);
}