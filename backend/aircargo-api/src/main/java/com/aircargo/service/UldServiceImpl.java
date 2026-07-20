package com.aircargo.service;

import com.aircargo.dto.UldAwbDTO;
import com.aircargo.dto.UldDTO;
import com.aircargo.common.entity.Airline;
import com.aircargo.entity.Flight;
import com.aircargo.entity.Uld;
import com.aircargo.entity.UldAwb;
import com.aircargo.repository.FlightRepository;
import com.aircargo.repository.UldAwbRepository;
import com.aircargo.repository.UldRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UldServiceImpl implements UldService{

    private static final BigDecimal KG_PER_LB = new BigDecimal("0.453592");

    private final UldRepository uldRepository;
    private final FlightRepository flightRepository;
    private final UldAwbRepository uldAwbRepository;

    public UldServiceImpl(UldRepository uldRepository, FlightRepository flightRepository, UldAwbRepository uldAwbRepository) {
        this.uldRepository = uldRepository;
        this.flightRepository = flightRepository;
        this.uldAwbRepository = uldAwbRepository;
    }

    private void computeMetricWeights(Uld e) {
        if (e.getTareLbs() != null) {
            e.setTareKg(e.getTareLbs().multiply(KG_PER_LB).setScale(2, RoundingMode.HALF_UP));
        }
        if (e.getGrossWeightLbs() != null) {
            e.setGrossWeightKg(e.getGrossWeightLbs().multiply(KG_PER_LB).setScale(2, RoundingMode.HALF_UP));
        }
        if (e.getGrossWeightLbs() != null && e.getTareLbs() != null) {
            BigDecimal netLbs = e.getGrossWeightLbs().subtract(e.getTareLbs());
            e.setNetWeightLbs(netLbs);
            e.setNetWeightKg(netLbs.multiply(KG_PER_LB).setScale(2, RoundingMode.HALF_UP));
        }
    }

    private UldDTO enrichWithAwbs(UldDTO dto) {
        if (dto == null || dto.getId() == null) return dto;
        List<UldAwbDTO> awbs = uldAwbRepository.findByUldId(dto.getId())
                .stream().map(UldAwbDTO::fromEntity).collect(Collectors.toList());
        dto.setAwbs(awbs);
        return dto;
    }

    @Override
    public List<UldDTO> getAll(UUID airlineId, UUID flightId) {
        List<Uld> results;
        if (flightId != null) results = uldRepository.findByFlightId(flightId);
        else if (airlineId != null) results = uldRepository.findByAirlineId(airlineId);
        else results = uldRepository.findAll();
        List<UldDTO> dtos = results.stream()
                .map(UldDTO::fromEntity)
                .collect(Collectors.toList());
        // Batch-load all UldAwb records in a single query
        List<UUID> uldIds = dtos.stream().map(UldDTO::getId).collect(Collectors.toList());
        if (!uldIds.isEmpty()) {
            List<UldAwb> allAwbs = uldAwbRepository.findByUldIdIn(uldIds);
            Map<UUID, List<UldAwbDTO>> awbMap = allAwbs.stream()
                    .collect(Collectors.groupingBy(
                            awb -> awb.getUld().getId(),
                            Collectors.mapping(UldAwbDTO::fromEntity, Collectors.toList())
                    ));
            dtos.forEach(dto -> dto.setAwbs(awbMap.getOrDefault(dto.getId(), List.of())));
        }
        return dtos;
    }

    @Override
    public Optional<UldDTO> getById(UUID id) {
        return uldRepository.findById(id)
                .map(UldDTO::fromEntity)
                .map(this::enrichWithAwbs);
    }

    @Override
    @Transactional
    public UldDTO create(UldDTO dto) {
        Uld e = UldDTO.toEntity(dto);
        computeMetricWeights(e);
        Uld saved = uldRepository.save(e);
        return enrichWithAwbs(UldDTO.fromEntity(saved));
    }

    @Override
    @Transactional
    public Optional<UldDTO> update(UUID id, UldDTO dto) {
        return uldRepository.findById(id)
                .map(existing -> {
                    if (dto.getAirlineId() != null) {
                        Airline airline = new Airline();
                        airline.setId(dto.getAirlineId());
                        existing.setAirline(airline);
                    }
                    if (dto.getFlightId() != null) {
                        Flight flight = new Flight();
                        flight.setId(dto.getFlightId());
                        existing.setFlight(flight);
                    }
                    if (dto.getUldNumber() != null) existing.setUldNumber(dto.getUldNumber());
                    if (dto.getUldType() != null) existing.setUldType(dto.getUldType());
                    if (dto.getPosition() != null) existing.setPosition(dto.getPosition());
                    if (dto.getConfig() != null) existing.setConfig(dto.getConfig());
                    if (dto.getSealNumber() != null) existing.setSealNumber(dto.getSealNumber());
                    if (dto.getTareLbs() != null) {
                        existing.setTareLbs(dto.getTareLbs());
                        existing.setTareKg(dto.getTareLbs().multiply(KG_PER_LB));
                    }
                    if (dto.getGrossWeightLbs() != null) {
                        existing.setGrossWeightLbs(dto.getGrossWeightLbs());
                        existing.setGrossWeightKg(dto.getGrossWeightLbs().multiply(KG_PER_LB));
                    }
                    if (dto.getNetWeightLbs() != null) {
                        existing.setNetWeightLbs(dto.getNetWeightLbs());
                        existing.setNetWeightKg(dto.getNetWeightLbs().multiply(KG_PER_LB));
                    }
                    if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
                    if (dto.getBuiltAt() != null) existing.setBuiltAt(dto.getBuiltAt());
                    if (dto.getLoadedAt() != null) existing.setLoadedAt(dto.getLoadedAt());
                    if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
                    return uldRepository.save(existing);
                })
                .map(UldDTO::fromEntity)
                .map(this::enrichWithAwbs);
    }

    @Override
    @Transactional
    public UldDTO transferUld(UUID uldId, UUID destinationFlightId, String reason) {
        Uld uld = uldRepository.findById(uldId)
                .orElseThrow(() -> new IllegalArgumentException("ULD not found: " + uldId));
        Flight destFlight = flightRepository.findById(destinationFlightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found: " + destinationFlightId));
        uld.setFlight(destFlight);
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String note = "[" + timestamp + "] Transferido a " + destFlight.getFlightNumber() + ": " + (reason != null ? reason : "");
        if (uld.getNotes() != null) {
            note = note + "\n" + uld.getNotes();
        }
        uld.setNotes(note);
        Uld saved = uldRepository.save(uld);
        return enrichWithAwbs(UldDTO.fromEntity(saved));
    }

    @Override
    @Transactional
    public UldDTO assignFlight(UUID id, UUID flightId) {
        Uld uld = uldRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ULD not found: " + id));
        if (flightId != null) {
            Flight flight = flightRepository.getReferenceById(flightId);
            uld.setFlight(flight);
        } else {
            uld.setFlight(null);
        }
        Uld saved = uldRepository.save(uld);
        return enrichWithAwbs(UldDTO.fromEntity(saved));
    }

    @Override
    @Transactional
    public boolean delete(UUID id) {
        if (!uldRepository.existsById(id)) return false;
        uldRepository.deleteById(id);
        return true;
    }
}
