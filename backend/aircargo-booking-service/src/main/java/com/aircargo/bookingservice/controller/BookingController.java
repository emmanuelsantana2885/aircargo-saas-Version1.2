package com.aircargo.bookingservice.controller;

import com.aircargo.bookingservice.dto.BookingAwbUpdateRequest;
import com.aircargo.bookingservice.dto.BookingDTO;
import com.aircargo.bookingservice.service.BookingService;
import com.aircargo.common.audit.AuditService;
import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.common.dto.PageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public List<BookingDTO> getAll(
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UUID flightId) {
        return bookingService.getAll(airlineId, flightId);
    }

    @GetMapping(params = {"page", "size"})
    public PageResponse<BookingDTO> getAllPaginated(
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UUID flightId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return bookingService.getAll(airlineId, flightId, page, size);
    }

    @GetMapping("/mawb/{mawbId}")
    public ResponseEntity<BookingDTO> getByMawbId(@PathVariable UUID mawbId) {
        return bookingService.findByMawbId(mawbId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/flight/{flightId}")
    public List<BookingDTO> getByFlight(@PathVariable UUID flightId) {
        return bookingService.getByFlightId(flightId);
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
        auditService.log(
                principal != null ? principal.getUserIdAsUuid() : null,
                principal != null ? principal.email() : "system",
                principal != null ? principal.fullName() : "system",
                "CREATE", "BOOKING", created.getId().toString(),
                "{\"awbNumber\":\"" + safe(created.getAwbNumber()) + "\",\"flightId\":\"" + (created.getFlightId() != null ? created.getFlightId().toString() : "") + "\"}",
                request.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingDTO> update(@PathVariable UUID id, @Valid @RequestBody BookingDTO dto,
                                              @AuthenticationPrincipal UserPrincipal principal,
                                              HttpServletRequest request) {
        return bookingService.update(id, dto)
                .map(updated -> {
                    auditService.log(
                            principal != null ? principal.getUserIdAsUuid() : null,
                            principal != null ? principal.email() : "system",
                            principal != null ? principal.fullName() : "system",
                            "UPDATE", "BOOKING", id.toString(),
                            "{\"awbNumber\":\"" + safe(updated.getAwbNumber()) + "\"}",
                            request.getRemoteAddr()
                    );
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
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
                    auditService.log(
                            principal != null ? principal.getUserIdAsUuid() : null,
                            principal != null ? principal.email() : "system",
                            principal != null ? principal.fullName() : "system",
                            "UPDATE_AWB", "BOOKING", id.toString(),
                            "{\"awbNumber\":\"" + safe(request.getAwbNumber()) + "\"}",
                            requestHttp.getRemoteAddr()
                    );
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
        auditService.log(
                principal != null ? principal.getUserIdAsUuid() : null,
                principal != null ? principal.email() : "system",
                principal != null ? principal.fullName() : "system",
                "DELETE", "BOOKING", id.toString(), null, request.getRemoteAddr()
        );
        return ResponseEntity.noContent().build();
    }

    private static String safe(String s) {
        return com.aircargo.common.util.TextUtil.safe(s);
    }
}
