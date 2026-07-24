package com.aircargo.repository;

import com.aircargo.entity.Flight;
import com.aircargo.entity.FlightStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FlightRepository extends JpaRepository<Flight, UUID> {

    List<Flight> findByAirlineId(UUID airlineid);

    Page<Flight> findByAirlineId(UUID airlineId, Pageable pageable);

    List<Flight> findByAirlineIdAndFlightDate(UUID airlineId, LocalDate flightDate);

    Page<Flight> findByAirlineIdAndFlightDate(UUID airlineId, LocalDate flightDate, Pageable pageable);

    List<Flight> findByAirlineIdAndStatus(UUID airlineId, FlightStatus status);

    Page<Flight> findByAirlineIdAndStatus(UUID airlineId, FlightStatus status, Pageable pageable);

    List<Flight> findByAirlineIdAndFlightNumber(UUID airlineId, String flightNumber);

    Page<Flight> findByAirlineIdAndFlightNumber(UUID airlineId, String flightNumber, Pageable pageable);

}
