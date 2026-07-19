package com.aircargo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.dto.BookingAwbUpdateRequest;
import com.aircargo.dto.BookingDTO;
import com.aircargo.service.AuditService;
import com.aircargo.service.BookingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

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
    public List<BookingDTO> getAll(
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UUID flightId) {
        return bookingService.getAll(airlineId, flightId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getById(@PathVariable UUID id) {
        return bookingService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<BookingDTO> create(@Valid @RequestBody BookingDTO dto,
                                              @AuthenticationPrincipal UserPrincipal principal,
                                              HttpServletRequest request) {
        BookingDTO created = bookingService.create(dto);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "CREATE", "BOOKING", created.getId().toString(),
                    "{\"awbNumber\":\"" + safe(created.getAwbNumber()) + "\",\"flightId\":\"" + (created.getFlightId() != null ? created.getFlightId().toString() : "") + "\"}",
                    request.getRemoteAddr());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDTO> update(@PathVariable UUID id, @Valid @RequestBody BookingDTO dto,
                                              @AuthenticationPrincipal UserPrincipal principal,
                                              HttpServletRequest request) {
        return bookingService.update(id, dto)
                .map(updated -> {
                    if (principal != null) {
                        auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                                "UPDATE", "BOOKING", id.toString(),
                                "{\"awbNumber\":\"" + safe(updated.getAwbNumber()) + "\"}",
                                request.getRemoteAddr());
                    }
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal,
                                        HttpServletRequest request) {
        boolean removed = bookingService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "DELETE", "BOOKING", id.toString(), null, request.getRemoteAddr());
        }
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/awb")
    public ResponseEntity<BookingDTO> updateAwb(@PathVariable UUID id,
                                                  @Valid @RequestBody BookingAwbUpdateRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal,
                                                  HttpServletRequest requestHttp) {
        if (request == null || request.getAwbNumber() == null || request.getAwbNumber().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return bookingService.updateAwb(id, request.getAwbNumber())
                .map(updated -> {
                    if (principal != null) {
                        auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                                "UPDATE", "BOOKING", id.toString(),
                                "{\"awbNumber\":\"" + safe(request.getAwbNumber()) + "\"}",
                                requestHttp.getRemoteAddr());
                    }
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private static String safe(String s) {
        return com.aircargo.common.util.TextUtil.safe(s);
    }
}
