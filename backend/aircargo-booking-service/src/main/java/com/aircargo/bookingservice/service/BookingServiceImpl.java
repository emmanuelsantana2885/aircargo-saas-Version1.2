package com.aircargo.bookingservice.service;

import com.aircargo.bookingservice.dto.BookingAwbUpdateRequest;
import com.aircargo.bookingservice.dto.BookingDTO;
import com.aircargo.bookingservice.dto.PageResponse;
import com.aircargo.bookingservice.entity.Booking;
import com.aircargo.bookingservice.entity.Flight;
import com.aircargo.bookingservice.repository.BookingRepository;
import com.aircargo.common.entity.Airline;
import com.aircargo.feign.client.FlightClient;
import com.aircargo.feign.client.MawbClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final FlightClient flightClient;
    private final MawbClient mawbClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:aircargo.events}")
    private String exchange;

    public BookingServiceImpl(BookingRepository bookingRepository, FlightClient flightClient,
                              MawbClient mawbClient, RabbitTemplate rabbitTemplate) {
        this.bookingRepository = bookingRepository;
        this.flightClient = flightClient;
        this.mawbClient = mawbClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Cacheable("bookings")
    public List<BookingDTO> getAll() {
        return bookingRepository.findAll().stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "bookings", key = "{#page, #size}")
    public PageResponse<BookingDTO> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> result = bookingRepository.findAll(pageable);
        return toPageResponse(result);
    }

    @Override
    @Cacheable(value = "bookings", key = "#id")
    public Optional<BookingDTO> getById(UUID id) {
        return bookingRepository.findById(id).map(BookingDTO::fromEntity);
    }

    @Override
    public Optional<BookingDTO> getByMawbId(UUID mawbId) {
        List<Booking> bookings = bookingRepository.findByMawbId(mawbId);
        if (bookings.isEmpty()) return Optional.empty();
        return Optional.of(BookingDTO.fromEntity(bookings.get(0)));
    }

    @Override
    @Cacheable(value = "bookings", key = "#flightId")
    public List<BookingDTO> getByFlightId(UUID flightId) {
        return bookingRepository.findByFlightId(flightId).stream()
                .map(BookingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "bookings", allEntries = true)
    public BookingDTO create(BookingDTO dto) {
        // Auto-resolve airline from flight if not provided
        if (dto.getAirlineId() == null && dto.getFlightId() != null) {
            try {
                var flight = flightClient.getFlightById(dto.getFlightId());
                if (flight != null) {
                    dto.setAirlineId(flight.getAirlineId());
                }
            } catch (Exception e) {
                // Flight service unavailable, proceed without airline
            }
        }

        Booking entity = BookingDTO.toEntity(dto);
        Booking saved = bookingRepository.save(entity);
        return BookingDTO.fromEntity(saved);
    }

    @Override
    @CacheEvict(value = "bookings", allEntries = true)
    public Optional<BookingDTO> update(UUID id, BookingDTO dto) {
        return bookingRepository.findById(id)
                .map(existing -> {
                    if (dto.getAirlineId() != null) {
                        Airline a = new Airline();
                        a.setId(dto.getAirlineId());
                        existing.setAirline(a);
                    }
                    if (dto.getFlightId() != null) {
                        Flight f = new Flight();
                        f.setId(dto.getFlightId());
                        existing.setFlight(f);
                    }
                    if (dto.getMawbId() != null) {
                        com.aircargo.bookingservice.entity.Mawb m = new com.aircargo.bookingservice.entity.Mawb();
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
                        BigDecimal fp = dto.getFulfillmentPct();
                        if (fp.compareTo(BigDecimal.valueOf(9999.9999)) > 0) {
                            fp = BigDecimal.valueOf(9999.9999);
                        }
                        existing.setFulfillmentPct(fp);
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
    @CacheEvict(value = "bookings", allEntries = true)
    public void updateAwb(UUID id, BookingAwbUpdateRequest request) {
        bookingRepository.findById(id).ifPresent(booking -> {
            booking.setAwbNumber(request.getAwbNumber());
            bookingRepository.save(booking);
            
            // Publish event for notification/export services
            try {
                var event = new com.aircargo.bookingservice.event.BookingAwbUpdatedEvent(
                        id, request.getAwbNumber(), booking.getFlight() != null ? booking.getFlight().getId() : null
                );
                rabbitTemplate.convertAndSend(exchange, "booking.awb.updated", event);
            } catch (Exception e) {
                // Log but don't fail the request
            }
        });
    }

    @Override
    @CacheEvict(value = "bookings", allEntries = true)
    public boolean delete(UUID id) {
        if (!bookingRepository.existsById(id)) return false;
        bookingRepository.deleteById(id);
        return true;
    }

    private PageResponse<BookingDTO> toPageResponse(Page<Booking> page) {
        return PageResponse.<BookingDTO>builder()
                .content(page.getContent().stream().map(BookingDTO::fromEntity).collect(Collectors.toList()))
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}