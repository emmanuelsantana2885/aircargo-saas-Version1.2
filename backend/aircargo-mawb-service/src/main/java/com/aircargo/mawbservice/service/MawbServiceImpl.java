package com.aircargo.mawbservice.service;

import com.aircargo.mawbservice.dto.MawbDTO;
import com.aircargo.mawbservice.entity.Mawb;
import com.aircargo.mawbservice.entity.MawbStatus;
import com.aircargo.mawbservice.repository.MawbRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MawbServiceImpl implements MawbService {

    private final MawbRepository mawbRepository;

    public MawbServiceImpl(MawbRepository mawbRepository) {
        this.mawbRepository = mawbRepository;
    }

    @Override
    @Cacheable(value = "mawbs", key = "{#airlineId, #flightId, #status, #page, #size}")
    public List<MawbDTO> getAll(UUID airlineId, UUID flightId, MawbStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Mawb> results;
        if (airlineId != null && flightId != null && status != null) {
            results = mawbRepository.findByAirlineIdAndStatus(airlineId, status);
            results = results.stream().filter(m -> m.getFlightId() != null && m.getFlightId().equals(flightId)).toList();
        } else if (airlineId != null && flightId != null) {
            results = mawbRepository.findByAirlineIdAndFlightId(airlineId, flightId);
        } else if (airlineId != null && status != null) {
            results = mawbRepository.findByAirlineIdAndStatus(airlineId, status);
        } else if (airlineId != null) {
            results = mawbRepository.findByAirlineId(airlineId);
        } else {
            results = mawbRepository.findAll();
        }
        int start = page * size;
        int end = Math.min(start + size, results.size());
        if (start >= results.size()) return List.of();
        return results.subList(start, end).stream()
                .map(MawbDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable("mawbs")
    public List<MawbDTO> getAll(UUID airlineId) {
        return mawbRepository.findByAirlineId(airlineId).stream()
                .map(MawbDTO::fromEntity)
                .toList();
    }

    @Override
    @Cacheable(value = "mawbs", key = "{#airlineId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<MawbDTO> getAll(UUID airlineId, Pageable pageable) {
        return mawbRepository.findByAirlineId(airlineId, pageable)
                .map(MawbDTO::fromEntity);
    }

    @Override
    @Cacheable(value = "mawbs", key = "#id")
    public Optional<MawbDTO> getById(UUID id) {
        return mawbRepository.findById(id).map(MawbDTO::fromEntity);
    }

    @Override
    public Optional<MawbDTO> getByAwbNumber(String awbNumber) {
        return mawbRepository.findByAwbNumber(awbNumber).map(MawbDTO::fromEntity);
    }

    @Override
    public List<MawbDTO> getByFlightId(UUID flightId) {
        return mawbRepository.findByFlightId(flightId).stream()
                .map(MawbDTO::fromEntity)
                .toList();
    }

    @Override
    @Cacheable(value = "mawbs", key = "{#flightId, #pageable.pageNumber, #pageable.pageSize}")
    public Page<MawbDTO> getByFlight(UUID flightId, Pageable pageable) {
        return mawbRepository.findByFlightId(flightId, pageable)
                .map(MawbDTO::fromEntity);
    }

    @Override
    @CacheEvict(value = "mawbs", allEntries = true)
    public MawbDTO create(MawbDTO dto) {
        Mawb entity = MawbDTO.toEntity(dto);
        entity.setStatus(MawbStatus.BOOKED);
        Mawb saved = mawbRepository.save(entity);
        return MawbDTO.fromEntity(saved);
    }

    @Override
    @CacheEvict(value = "mawbs", allEntries = true)
    public Optional<MawbDTO> update(UUID id, MawbDTO dto) {
        return mawbRepository.findById(id)
                .map(existing -> {
                    if (dto.getAirlineId() != null) existing.setAirlineId(dto.getAirlineId());
                    if (dto.getFlightId() != null) existing.setFlightId(dto.getFlightId());
                    if (dto.getAwbNumber() != null) existing.setAwbNumber(dto.getAwbNumber());
                    if (dto.getShipperName() != null) existing.setShipperName(dto.getShipperName());
                    if (dto.getConsigneeName() != null) existing.setConsigneeName(dto.getConsigneeName());
                    if (dto.getOrigin() != null) existing.setOrigin(dto.getOrigin());
                    if (dto.getDestination() != null) existing.setDestination(dto.getDestination());
                    if (dto.getPieces() != null) existing.setPieces(dto.getPieces());
                    if (dto.getReportedWeightKg() != null) existing.setReportedWeightKg(dto.getReportedWeightKg());
                    if (dto.getChargeableWeightKg() != null) existing.setChargeableWeightKg(dto.getChargeableWeightKg());
                    if (dto.getCommodityType() != null) existing.setCommodityType(dto.getCommodityType());
                    if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
                    if (dto.getCashOnly() != null) existing.setCashOnly(dto.getCashOnly());
                    if (dto.getBookedInAcoms() != null) existing.setBookedInAcoms(dto.getBookedInAcoms());
                    if (dto.getDocsProvided() != null) existing.setDocsProvided(dto.getDocsProvided());
                    if (dto.getCustomsCompleted() != null) existing.setCustomsCompleted(dto.getCustomsCompleted());
                    if (dto.getPreBuilt() != null) existing.setPreBuilt(dto.getPreBuilt());
                    if (dto.getLooseTender() != null) existing.setLooseTender(dto.getLooseTender());
                    if (dto.getSupportingDocs() != null) existing.setSupportingDocs(dto.getSupportingDocs());
                    if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
                    return mawbRepository.save(existing);
                })
                .map(MawbDTO::fromEntity);
    }

    @Override
    @CacheEvict(value = "mawbs", allEntries = true)
    public Optional<MawbDTO> updateStatus(UUID id, MawbStatus status) {
        return mawbRepository.findById(id)
                .map(existing -> {
                    existing.setStatus(status);
                    return mawbRepository.save(existing);
                })
                .map(MawbDTO::fromEntity);
    }

    @Override
    @CacheEvict(value = "mawbs", allEntries = true)
    public boolean delete(UUID id) {
        if (!mawbRepository.existsById(id)) return false;
        mawbRepository.deleteById(id);
        return true;
    }
}