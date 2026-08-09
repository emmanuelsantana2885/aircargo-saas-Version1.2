package com.aircargo.uldservice.service;

import com.aircargo.uldservice.dto.UldTypeConfigDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UldTypeConfigService {
    List<UldTypeConfigDTO> getAll(UUID airlineId);
    Optional<UldTypeConfigDTO> getById(UUID id);
    UldTypeConfigDTO create(UldTypeConfigDTO dto);
    Optional<UldTypeConfigDTO> update(UUID id, UldTypeConfigDTO dto);
    boolean delete(UUID id);
    List<UldTypeConfigDTO> replaceAllForAirline(UUID airlineId, List<UldTypeConfigDTO> dtos);
}
