package com.aircargo.service;

import com.aircargo.dto.AppUserDTO;
import com.aircargo.entity.AppUser;
import com.aircargo.entity.Site;
import com.aircargo.repository.AppUserRepository;
import com.aircargo.common.entity.Airline;
import com.aircargo.repository.SiteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository repository;
    private final SiteRepository siteRepository;

    public AppUserServiceImpl(AppUserRepository repository, SiteRepository siteRepository) {
        this.repository = repository;
        this.siteRepository = siteRepository;
    }

    @Override
    public List<AppUserDTO> getAll(UUID airlineId) {
        List<AppUser> results = airlineId != null ? repository.findByAirlineId(airlineId) : repository.findAll();
        return results.stream().map(AppUserDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public Optional<AppUserDTO> getById(UUID id) {
        return repository.findById(id).map(AppUserDTO::fromEntity);
    }

    @Override
    @Transactional
    public AppUserDTO create(AppUserDTO dto) {
        AppUser e = AppUserDTO.toEntity(dto);
        e.setPasswordHash(null);
        e.setMustChangePassword(true);
        if (dto.getSiteIds() != null) {
            Set<Site> sites = dto.getSiteIds().stream()
                    .map(siteRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            e.setSites(sites);
        }
        AppUser saved = repository.save(e);
        return AppUserDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public Optional<AppUserDTO> update(UUID id, AppUserDTO dto) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setEmail(dto.getEmail());
                    existing.setFullName(dto.getFullName());
                    existing.setRole(dto.getRole());
                    existing.setIsActive(dto.getIsActive());
                    if (dto.getAirlineId() != null) {
                        Airline airline = new Airline();
                        airline.setId(dto.getAirlineId());
                        existing.setAirline(airline);
                    }
                    if (dto.getSiteIds() != null) {
                        Set<Site> sites = dto.getSiteIds().stream()
                                .map(siteRepository::findById)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toSet());
                        existing.setSites(sites);
                    }
                    return repository.save(existing);
                })
                .map(AppUserDTO::fromEntity);
    }

    @Override
    @Transactional
    public boolean delete(UUID id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public void resetPassword(UUID id) {
        repository.findById(id).ifPresent(user -> {
            user.setPasswordHash(null);
            user.setMustChangePassword(true);
            repository.save(user);
        });
    }
}
