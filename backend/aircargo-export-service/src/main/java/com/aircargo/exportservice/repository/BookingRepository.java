package com.aircargo.exportservice.repository;

import com.aircargo.exportservice.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {
    List<BookingEntity> findByFlightId(UUID flightId);
}
