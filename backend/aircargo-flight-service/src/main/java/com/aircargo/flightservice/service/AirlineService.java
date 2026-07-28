package com.aircargo.flightservice.service;

import com.aircargo.flightservice.dto.AirlineDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AirlineService {
    List<AirlineDTO> getAll();
    Optional<AirlineDTO> getById(UUID id);
    AirlineDTO create(AirlineDTO dto);
    Optional<AirlineDTO> update(UUID id, AirlineDTO dto);
    boolean delete(UUID id);
}