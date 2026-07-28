package com.aircargo.mawbservice.service;

import com.aircargo.common.dto.PageResponse;
import com.aircargo.mawbservice.dto.MawbDTO;
import com.aircargo.mawbservice.entity.MawbStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface MawbService {
    List<MawbDTO> getAll(UUID airlineId, UUID flightId, MawbStatus status);
    PageResponse<MawbDTO> getAll(UUID airlineId, UUID flightId, MawbStatus status, int page, int size);
    Optional<MawbDTO> getById(UUID id);
    Optional<MawbDTO> getByAwbNumber(String awbNumber);
    List<MawbDTO> getByFlightId(UUID flightId);
    MawbDTO create(MawbDTO dto);
    Optional<MawbDTO> update(UUID id, MawbDTO dto);
    Optional<MawbDTO> updateStatus(UUID id, MawbStatus status);
    void updateSupportingDocs(UUID id, Map<String, Object> body);
    byte[] getSupportingDocsPdf(UUID id);
    boolean delete(UUID id);
}
