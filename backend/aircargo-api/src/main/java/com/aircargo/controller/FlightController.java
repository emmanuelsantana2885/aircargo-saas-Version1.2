package com.aircargo.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.dto.FlightDTO;
import com.aircargo.dto.PageResponse;
import com.aircargo.entity.FlightStatus;
import com.aircargo.service.AuditService;
import com.aircargo.service.FlightService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;
    private final AuditService auditService;

    public FlightController(FlightService flightService, AuditService auditService){
        this.flightService = flightService;
        this.auditService = auditService;
    }

    // GET /api/flights?airlineId=xxx&page=0&size=50
    @GetMapping
    public PageResponse<FlightDTO> getAll(
            @RequestParam(required=false) UUID airlineId,
            @RequestParam(required =false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) FlightStatus status,
            @RequestParam(required = false) String flightNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        return flightService.getAll(airlineId, date, status, flightNumber, page, size);
    }

    // GET /api/flights/{id}
    @GetMapping("/{id}")
    public ResponseEntity<FlightDTO> getById(@PathVariable UUID id) {
        return flightService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/flights
    @PostMapping
    public ResponseEntity<FlightDTO> create(@Valid @RequestBody FlightDTO dto,
                                             @AuthenticationPrincipal UserPrincipal principal,
                                             HttpServletRequest request){
        FlightDTO created = flightService.create(dto);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "CREATE", "FLIGHT", created.getId().toString(),
                    "{\"flightNumber\":\"" + safe(created.getFlightNumber()) + "\",\"airlineId\":\"" + created.getAirlineId() + "\"}",
                    request.getRemoteAddr());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // PUT /api/flights/{id}
    @PutMapping("/{id}")
    public ResponseEntity<FlightDTO> update(@PathVariable UUID id, @Valid @RequestBody FlightDTO dto,
                                             @AuthenticationPrincipal UserPrincipal principal,
                                             HttpServletRequest request){
        return flightService.update(id, dto)
                .map(updated -> {
                    if (principal != null) {
                        auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                                "UPDATE", "FLIGHT", id.toString(),
                                "{\"flightNumber\":\"" + safe(updated.getFlightNumber()) + "\"}",
                                request.getRemoteAddr());
                    }
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/flights/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal,
                                        HttpServletRequest request){
        boolean removed = flightService.delete(id);
        if(!removed) return ResponseEntity.notFound().build();
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "DELETE", "FLIGHT", id.toString(), null, request.getRemoteAddr());
        }
        return ResponseEntity.noContent().build();
    }

    private static String safe(String s) {
        return com.aircargo.common.util.TextUtil.safe(s);
    }

}
