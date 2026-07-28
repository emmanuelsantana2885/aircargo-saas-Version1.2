package com.aircargo.flightservice.service;

import com.aircargo.common.entity.Airline;
import com.aircargo.flightservice.dto.AirlineDTO;
import com.aircargo.flightservice.repository.AirlineRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AirlineServiceImpl implements AirlineService {

    private final AirlineRepository repository;

    public AirlineServiceImpl(AirlineRepository repository) {
        this.repository = repository;
    }

    @Override
    @Cacheable("airlines")
    public List<AirlineDTO> getAll() {
        return repository.findAll().stream().map(AirlineDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "airlines", key = "#id")
    public Optional<AirlineDTO> getById(UUID id) {
        return repository.findById(id).map(AirlineDTO::fromEntity);
    }

    @Override
    @CacheEvict(value = "airlines", allEntries = true)
    public AirlineDTO create(AirlineDTO dto) {
        Airline e = AirlineDTO.toEntity(dto);
        Airline saved = repository.save(e);
        return AirlineDTO.fromEntity(saved);
    }

    @Override
    @CacheEvict(value = "airlines", allEntries = true)
    public Optional<AirlineDTO> update(UUID id, AirlineDTO dto) {
        return repository.findById(id)
                .map(existing -> {
                    Airline updated = AirlineDTO.toEntity(dto);
                    updated.setId(existing.getId());
                    return repository.save(updated);
                })
                .map(AirlineDTO::fromEntity);
    }

    @Override
    @CacheEvict(value = "airlines", allEntries = true)
    public boolean delete(UUID id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}