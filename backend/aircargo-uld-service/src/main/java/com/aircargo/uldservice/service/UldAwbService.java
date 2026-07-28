package com.aircargo.uldservice.service;

import com.aircargo.uldservice.dto.UldAwbDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UldAwbService {
    List<UldAwbDTO> getAll(UUID uldId, UUID mawbId);
    Optional<UldAwbDTO> getById(UUID id);
    UldAwbDTO create(UldAwbDTO dto);
    Optional<UldAwbDTO> update(UUID id, UldAwbDTO dto);
    boolean delete(UUID id);
}
