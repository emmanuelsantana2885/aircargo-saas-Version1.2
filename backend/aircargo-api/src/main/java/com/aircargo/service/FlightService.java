package com.aircargo.service;

import com.aircargo.dto.FlightDTO;
import com.aircargo.dto.PageResponse;
import com.aircargo.entity.FlightStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlightService {

    PageResponse<FlightDTO> getAll(UUID airlineId, LocalDate flightDate, FlightStatus status, String flightNumber, int page, int size);

    List<FlightDTO> getAll(UUID airlineId, LocalDate flightDate, FlightStatus status, String flightNumber);

    Optional<FlightDTO> getById(UUID id);

    Optional<FlightDTO> getByAirlineIdAndFlightNumber(UUID airlineId, String flightNumber);

    Optional<FlightDTO> updateStatus(UUID id, FlightStatus status);
    
    FlightDTO create(FlightDTO dto);

    Optional<FlightDTO> update(UUID id, FlightDTO dto);

    boolean delete(UUID id);
}
