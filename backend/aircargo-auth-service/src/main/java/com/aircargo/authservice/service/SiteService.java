package com.aircargo.authservice.service;

import com.aircargo.authservice.dto.SiteDTO;
import com.aircargo.authservice.entity.Site;
import com.aircargo.authservice.repository.SiteRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SiteService {

    private final SiteRepository repository;

    public SiteService(SiteRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = "sites")
    public List<SiteDTO> getAll() {
        return repository.findAll().stream()
                .map(SiteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "sites", key = "'active'")
    public List<SiteDTO> getActive() {
        return repository.findByIsActiveTrue().stream()
                .map(SiteDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "sites", key = "#id")
    public Optional<SiteDTO> getById(UUID id) {
        return repository.findById(id).map(SiteDTO::fromEntity);
    }

    @CacheEvict(value = "sites", allEntries = true)
    public SiteDTO create(SiteDTO dto) {
        Site entity = SiteDTO.toEntity(dto);
        Site saved = repository.save(entity);
        return SiteDTO.fromEntity(saved);
    }

    @CacheEvict(value = "sites", allEntries = true)
    public Optional<SiteDTO> update(UUID id, SiteDTO dto) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setCode(dto.getCode());
                    existing.setName(dto.getName());
                    existing.setCountry(dto.getCountry());
                    existing.setIsActive(dto.getIsActive());
                    return repository.save(existing);
                })
                .map(SiteDTO::fromEntity);
    }

    @CacheEvict(value = "sites", allEntries = true)
    public boolean delete(UUID id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
