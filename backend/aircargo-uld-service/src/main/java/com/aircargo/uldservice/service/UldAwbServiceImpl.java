package com.aircargo.uldservice.service;

import com.aircargo.uldservice.dto.UldAwbDTO;
import com.aircargo.uldservice.entity.UldAwb;
import com.aircargo.uldservice.repository.UldAwbRepository;
import com.aircargo.uldservice.repository.UldRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UldAwbServiceImpl implements UldAwbService {

    private final UldAwbRepository uldAwbRepository;
    private final UldRepository uldRepository;

    public UldAwbServiceImpl(UldAwbRepository uldAwbRepository,
                              UldRepository uldRepository) {
        this.uldAwbRepository = uldAwbRepository;
        this.uldRepository = uldRepository;
    }

    @Override
    @Cacheable(value = "uld-awbs", key = "{#uldId, #mawbId}")
    public List<UldAwbDTO> getAll(UUID uldId, UUID mawbId) {
        List<UldAwb> results;
        if (uldId != null) {
            results = uldAwbRepository.findByUldId(uldId);
        } else if (mawbId != null) {
            results = uldAwbRepository.findByMawbId(mawbId);
        } else {
            results = uldAwbRepository.findAll();
        }
        return results.stream()
                .map(UldAwbDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "uld-awbs", key = "#id")
    public Optional<UldAwbDTO> getById(UUID id) {
        return uldAwbRepository.findById(id)
                .map(UldAwbDTO::fromEntity);
    }

    @Override
    @CacheEvict(value = {"uld-awbs", "ulds"}, allEntries = true)
    public UldAwbDTO create(UldAwbDTO dto) {
        if (!uldRepository.existsById(dto.getUldId())) {
            throw new IllegalArgumentException("ULD not found: " + dto.getUldId());
        }
        UldAwb entity = UldAwbDTO.toEntity(dto);
        entity.setId(null);
        UldAwb saved = uldAwbRepository.save(entity);
        return UldAwbDTO.fromEntity(saved);
    }

    @Override
    @CacheEvict(value = {"uld-awbs", "ulds"}, allEntries = true)
    public Optional<UldAwbDTO> update(UUID id, UldAwbDTO dto) {
        return uldAwbRepository.findById(id)
                .map(existing -> {
                    UldAwb updated = UldAwbDTO.toEntity(dto);
                    updated.setId(existing.getId());
                    return uldAwbRepository.save(updated);
                })
                .map(UldAwbDTO::fromEntity);
    }

    @Override
    @CacheEvict(value = {"uld-awbs", "ulds"}, allEntries = true)
    public boolean delete(UUID id) {
        if (!uldAwbRepository.existsById(id)) return false;
        uldAwbRepository.deleteById(id);
        return true;
    }
}
