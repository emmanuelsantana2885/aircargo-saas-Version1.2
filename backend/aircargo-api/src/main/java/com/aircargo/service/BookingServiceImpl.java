package com.aircargo.service;

import com.aircargo.dto.BookingDTO;
import com.aircargo.entity.Booking;
import com.aircargo.common.entity.Airline;
import com.aircargo.entity.Flight;
import com.aircargo.repository.AirlineRepository;
import com.aircargo.repository.BookingRepository;
import com.aircargo.repository.FlightRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService{

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final AirlineRepository airlineRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              FlightRepository flightRepository,
                              AirlineRepository airlineRepository) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.airlineRepository = airlineRepository;
    }

    @Override
    public List<BookingDTO> getAll(UUID airlineId, UUID flightId) {
        List<Booking> results;
        if (flightId != null) results = bookingRepository.findByFlightId(flightId);
        else if (airlineId != null) results = bookingRepository.findByAirlineId(airlineId);
        else results = bookingRepository.findAll();
        return results.stream().map(BookingDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public Optional<BookingDTO> getById(UUID id) {
        return bookingRepository.findById(id).map(BookingDTO::fromEntity);
    }

    @Override
    @Transactional
    public BookingDTO create(BookingDTO dto) {
        // Auto-resolve airline from Flight if not provided
        if (dto.getAirlineId() == null && dto.getFlightId() != null) {
            flightRepository.findById(dto.getFlightId()).ifPresent(f -> {
                if (f.getAirline() != null) dto.setAirlineId(f.getAirline().getId());
            });
        }
        Booking entity = BookingDTO.toEntity(dto);
        Booking saved = bookingRepository.save(entity);
        return BookingDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public Optional<BookingDTO> update(UUID id, BookingDTO dto) {
        return bookingRepository.findById(id)
                .map(existing -> {
                    if (dto.getAirlineId() != null) {
                        Airline a = new Airline();
                        a.setId(dto.getAirlineId());
                        existing.setAirline(a);
                    }
                    if (dto.getFlightId() != null) {
                        com.aircargo.entity.Flight f = new com.aircargo.entity.Flight();
                        f.setId(dto.getFlightId());
                        existing.setFlight(f);
                    }
                    if (dto.getMawbId() != null) {
                        com.aircargo.entity.Mawb m = new com.aircargo.entity.Mawb();
                        m.setId(dto.getMawbId());
                        existing.setMawb(m);
                    }
                    if (dto.getClientName() != null) existing.setClientName(dto.getClientName());
                    if (dto.getContactName() != null) existing.setContactName(dto.getContactName());
                    if (dto.getCnee() != null) existing.setCnee(dto.getCnee());
                    if (dto.getShipperName() != null) existing.setShipperName(dto.getShipperName());
                    if (dto.getAwbNumber() != null) existing.setAwbNumber(dto.getAwbNumber());
                    if (dto.getSkids() != null) existing.setSkids(dto.getSkids());
                    if (dto.getUnits() != null) existing.setUnits(dto.getUnits());
                    if (dto.getReservedKg() != null) existing.setReservedKg(dto.getReservedKg());
                    if (dto.getConfirmedKg() != null) existing.setConfirmedKg(dto.getConfirmedKg());
                    if (dto.getReceivedKg() != null) existing.setReceivedKg(dto.getReceivedKg());
                    if (dto.getFulfillmentPct() != null) {
                        java.math.BigDecimal val = dto.getFulfillmentPct();
                        if (val.compareTo(new java.math.BigDecimal("9999.9999")) > 0) val = new java.math.BigDecimal("9999.9999");
                        existing.setFulfillmentPct(val);
                    }
                    if (dto.getDestination() != null) existing.setDestination(dto.getDestination());
                    if (dto.getPriority() != null) existing.setPriority(dto.getPriority());
                    if (dto.getCommodityType() != null) existing.setCommodityType(dto.getCommodityType());
                    if (dto.getDayReceived() != null) existing.setDayReceived(dto.getDayReceived());
                    if (dto.getTimeHours() != null) existing.setTimeHours(dto.getTimeHours());
                    if (dto.getPositions() != null) existing.setPositions(dto.getPositions());
                    if (dto.getRealPositions() != null) existing.setRealPositions(dto.getRealPositions());
                    if (dto.getLastWeekKg() != null) existing.setLastWeekKg(dto.getLastWeekKg());
                    if (dto.getLastWeekPositions() != null) existing.setLastWeekPositions(dto.getLastWeekPositions());
                    if (dto.getIsConfirmed() != null) existing.setIsConfirmed(dto.getIsConfirmed());
                    if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
                    return bookingRepository.save(existing);
                })
                .map(BookingDTO::fromEntity);
    }

    @Override
    @Transactional
    public boolean delete(UUID id) {
        if (!bookingRepository.existsById(id)) return false;
        bookingRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public Optional<BookingDTO> updateAwb(UUID id, String awbNumber) {
        return bookingRepository.findById(id)
                .map(existing -> {
                    existing.setAwbNumber(awbNumber);
                    return bookingRepository.save(existing);
                })
                .map(BookingDTO::fromEntity);
    }
}
