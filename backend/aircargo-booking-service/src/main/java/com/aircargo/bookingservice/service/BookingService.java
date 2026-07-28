package com.aircargo.bookingservice.service;

import com.aircargo.bookingservice.dto.BookingAwbUpdateRequest;
import com.aircargo.bookingservice.dto.BookingDTO;
import com.aircargo.bookingservice.dto.PageResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingService {
    List<BookingDTO> getAll();
    PageResponse<BookingDTO> getAll(int page, int size);
    Optional<BookingDTO> getById(UUID id);
    Optional<BookingDTO> getByMawbId(UUID mawbId);
    List<BookingDTO> getByFlightId(UUID flightId);
    BookingDTO create(BookingDTO dto);
    Optional<BookingDTO> update(UUID id, BookingDTO dto);
    void updateAwb(UUID id, BookingAwbUpdateRequest request);
    boolean delete(UUID id);
}