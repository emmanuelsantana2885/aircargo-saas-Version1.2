package com.aircargo.flightservice.service;

import com.aircargo.flightservice.dto.FlightDTO;
import com.aircargo.flightservice.entity.FlightStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;

public interface FlightService {

    List<FlightDTO> getAll(UUID airlineId, LocalDate flightDate, FlightStatus status, String flightNumber);

    Page<FlightDTO> getAll(UUID airlineId, LocalDate flightDate, FlightStatus status, String flightNumber, int page, int size);

    Optional<FlightDTO> getById(UUID id);

    Optional<FlightDTO> getByAirlineIdAndFlightNumber(UUID airlineId, String flightNumber);

    Optional<FlightDTO> updateStatus(UUID id, FlightStatus status);

    FlightDTO create(FlightDTO dto);

    Optional<FlightDTO> update(UUID id, FlightDTO dto);

    boolean delete(UUID id);
}
