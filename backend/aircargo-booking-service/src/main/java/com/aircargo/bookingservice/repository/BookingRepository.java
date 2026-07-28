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
    Page<Booking> findAll(Pageable pageable);
}