package com.aircargo.mawbservice.service;

import com.aircargo.mawbservice.dto.MawbDTO;
import com.aircargo.mawbservice.entity.MawbStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MawbService {
    List<MawbDTO> getAll(UUID airlineId, UUID flightId, MawbStatus status, int page, int size);
    List<MawbDTO> getAll(UUID airlineId);
    Page<MawbDTO> getAll(UUID airlineId, Pageable pageable);
    Optional<MawbDTO> getById(UUID id);
    Optional<MawbDTO> getByAwbNumber(String awbNumber);
    Page<MawbDTO> getByFlight(UUID flightId, Pageable pageable);
    List<MawbDTO> getByFlightId(UUID flightId);
    MawbDTO create(MawbDTO dto);
    Optional<MawbDTO> update(UUID id, MawbDTO dto);
    Optional<MawbDTO> updateStatus(UUID id, MawbStatus status);
    boolean delete(UUID id);
}