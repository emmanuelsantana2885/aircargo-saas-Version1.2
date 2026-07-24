package com.aircargo.service;

import com.aircargo.dto.FlightDTO;
import com.aircargo.dto.PageResponse;
import com.aircargo.entity.Flight;
import com.aircargo.common.entity.Airline;
import com.aircargo.entity.FlightStatus;
import com.aircargo.repository.FlightRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FlightServiceImpl implements FlightService{

    private final FlightRepository flightRepository;

    public FlightServiceImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "flights", key = "{#airlineId, #flightNumber, #flightDate, #status}")
    public List<FlightDTO> getAll(UUID airlineId, LocalDate flightDate, FlightStatus status, String flightNumber) {
        List<Flight> results;
        if (flightNumber != null && airlineId != null) {
            results = flightRepository.findByAirlineIdAndFlightNumber(airlineId, flightNumber);
        } else if (flightDate != null && airlineId != null) {
            results = flightRepository.findByAirlineIdAndFlightDate(airlineId, flightDate);
        } else if (status != null && airlineId != null) {
            results = flightRepository.findByAirlineIdAndStatus(airlineId, status);
        } else if (airlineId != null) {
            results = flightRepository.findByAirlineId(airlineId);
        } else {
            results = flightRepository.findAll();
        }
        return results.stream()
                .map(FlightDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<FlightDTO> getAll(UUID airlineId, LocalDate flightDate, FlightStatus status, String flightNumber, int page, int size) {
        PageRequest pageReq = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "flightDate"));
        Page<Flight> result;

        if (flightNumber != null && airlineId != null) {
            result = flightRepository.findByAirlineIdAndFlightNumber(airlineId, flightNumber, pageReq);
        } else if (flightDate != null && airlineId != null) {
            result = flightRepository.findByAirlineIdAndFlightDate(airlineId, flightDate, pageReq);
        } else if (status != null && airlineId != null) {
            result = flightRepository.findByAirlineIdAndStatus(airlineId, status, pageReq);
        } else if (airlineId != null) {
            result = flightRepository.findByAirlineId(airlineId, pageReq);
        } else {
            result = flightRepository.findAll(pageReq);
        }

        List<FlightDTO> dtoList = result.getContent().stream()
                .map(FlightDTO::fromEntity)
                .collect(Collectors.toList());

        return PageResponse.of(dtoList, page, size, result.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FlightDTO> getById(UUID id) {
        return flightRepository.findById(id).map(FlightDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FlightDTO> getByAirlineIdAndFlightNumber(UUID airlineId, String flightNumber) {
        List<Flight> list = flightRepository.findByAirlineIdAndFlightNumber(airlineId, flightNumber);
        if (list == null || list.isEmpty()) return Optional.empty();
        return Optional.ofNullable(FlightDTO.fromEntity(list.get(0)));
    }

    @Override
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public Optional<FlightDTO> updateStatus(UUID id, FlightStatus status) {
        return flightRepository.findById(id)
                .map(f -> {
                    f.setStatus(status);
                    return flightRepository.save(f);
                })
                .map(FlightDTO::fromEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public FlightDTO create(FlightDTO dto) {
        Flight entity = FlightDTO.toEntity(dto);
        Flight saved = flightRepository.save(entity);
        return FlightDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public Optional<FlightDTO> update(UUID id, FlightDTO dto) {
        return flightRepository.findById(id)
                .map(existing -> {
                    if (dto.getAirlineId() != null) {
                        Airline a = new Airline();
                        a.setId(dto.getAirlineId());
                        existing.setAirline(a);
                    }
                    if (dto.getFlightNumber() != null) existing.setFlightNumber(dto.getFlightNumber());
                    if (dto.getOrigin() != null) existing.setOrigin(dto.getOrigin());
                    if (dto.getDestination() != null) existing.setDestination(dto.getDestination());
                    if (dto.getAircraftReg() != null) existing.setAircraftReg(dto.getAircraftReg());
                    if (dto.getAircraftType() != null) existing.setAircraftType(dto.getAircraftType());
                    if (dto.getFlightDate() != null) existing.setFlightDate(dto.getFlightDate());
                    if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
                    if (dto.getMaxPayloadKg() != null) existing.setMaxPayloadKg(dto.getMaxPayloadKg());
                    if (dto.getTotalPositions() != null) existing.setTotalPositions(dto.getTotalPositions());
                    if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
                    return flightRepository.save(existing);
                })
                .map(FlightDTO::fromEntity);
    }

    @Override
    @Transactional
    @CacheEvict(value = "flights", allEntries = true)
    public boolean delete(UUID id) {
        if (!flightRepository.existsById(id)) return false;
        flightRepository.deleteById(id);
        return true;
    }
}
