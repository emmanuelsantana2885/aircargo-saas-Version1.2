package com.aircargo.authservice.repository;

import com.aircargo.common.entity.Airline;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AirlineRepository extends JpaRepository<Airline, UUID> {
    Optional<Airline> findByCode(String code);
}
