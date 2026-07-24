package com.aircargo.service;

import com.aircargo.dto.PageResponse;
import com.aircargo.dto.UldDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UldService {
    List<UldDTO> getAll(UUID airlineId, UUID flightId);
    PageResponse<UldDTO> getAll(UUID airlineId, UUID flightId, int page, int size);
    Optional<UldDTO> getById(UUID id);
    UldDTO create(UldDTO dto);
    Optional<UldDTO> update(UUID id, UldDTO dto);
    UldDTO assignFlight(UUID id, UUID flightId);
    UldDTO transferUld(UUID uldId, UUID destinationFlightId, String reason);
    boolean delete(UUID id);
}
