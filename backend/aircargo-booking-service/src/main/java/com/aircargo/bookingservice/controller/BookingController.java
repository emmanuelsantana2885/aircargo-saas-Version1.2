package com.aircargo.bookingservice.controller;

import com.aircargo.bookingservice.dto.BookingAwbUpdateRequest;
import com.aircargo.bookingservice.dto.BookingDTO;
import com.aircargo.bookingservice.dto.PageResponse;
import com.aircargo.bookingservice.service.BookingService;
import com.aircargo.bookingservice.service.AuditService;
import com.aircargo.common.auth.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final AuditService auditService;

    public BookingController(BookingService bookingService, AuditService auditService) {
        this.bookingService = bookingService;
        this.auditService = auditService;
    }

    @GetMapping
    public List<BookingDTO> getAll() {
        return bookingService.getAll();
    }

    @GetMapping("/paged")
    public PageResponse<BookingDTO> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return bookingService.getAll(page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getById(@PathVariable UUID id) {
        return bookingService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/mawb/{mawbId}")
    public ResponseEntity<BookingDTO> getByMawbId(@PathVariable UUID mawbId) {
        return bookingService.getByMawbId(mawbId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/flight/{flightId}")
    public List<BookingDTO> getByFlightId(@PathVariable UUID flightId) {
        return bookingService.getByFlightId(flightId);
    }

    @PostMapping
    public ResponseEntity<BookingDTO> create(@Valid @RequestBody BookingDTO dto,
                                              @AuthenticationPrincipal UserPrincipal principal,
                                              HttpServletRequest request) {
        BookingDTO created = bookingService.create(dto);
        auditService.log(
                principal != null ? principal.getUserIdAsUuid() : null,
                principal != null ? principal.email() : "system",
                principal != null ? principal.fullName() : "system",
                "CREATE", "BOOKING", created.getId().toString(),
                "{\"flightId\":\"" + created.getFlightId() + "\"}",
                request.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDTO> update(@PathVariable UUID id, @Valid @RequestBody BookingDTO dto,
                                              @AuthenticationPrincipal UserPrincipal principal,
                                              HttpServletRequest request) {
        Optional<BookingDTO> updated = bookingService.update(id, dto);
        if (updated.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        auditService.log(
                principal != null ? principal.getUserIdAsUuid() : null,
                principal != null ? principal.email() : "system",
                principal != null ? principal.fullName() : "system",
                "UPDATE", "BOOKING", id.toString(),
                "{\"flightId\":\"" + dto.getFlightId() + "\"}",
                request.getRemoteAddr()
        );
        return ResponseEntity.ok(updated.get());
    }

    @PatchMapping("/{id}/awb")
    public ResponseEntity<Void> updateAwb(@PathVariable UUID id,
                                           @Valid @RequestBody BookingAwbUpdateRequest request,
                                           @AuthenticationPrincipal UserPrincipal principal,
                                           HttpServletRequest servletRequest) {
        bookingService.updateAwb(id, request);
        auditService.log(
                principal != null ? principal.getUserIdAsUuid() : null,
                principal != null ? principal.email() : "system",
                principal != null ? principal.fullName() : "system",
                "UPDATE_AWB", "BOOKING", id.toString(),
                "{\"awbNumber\":\"" + request.getAwbNumber() + "\"}",
                servletRequest.getRemoteAddr()
        );
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal,
                                        HttpServletRequest request) {
        boolean removed = bookingService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        auditService.log(
                principal != null ? principal.getUserIdAsUuid() : null,
                principal != null ? principal.email() : "system",
                principal != null ? principal.fullName() : "system",
                "DELETE", "BOOKING", id.toString(), null, request.getRemoteAddr()
        );
        return ResponseEntity.noContent().build();
    }
}