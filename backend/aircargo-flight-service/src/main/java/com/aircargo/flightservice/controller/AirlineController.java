package com.aircargo.flightservice.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.flightservice.dto.AirlineDTO;
import com.aircargo.flightservice.service.AirlineService;
import com.aircargo.flightservice.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/airlines")
public class AirlineController {

    private final AirlineService airlineService;
    private final AuditService auditService;

    public AirlineController(AirlineService airlineService, AuditService auditService) {
        this.airlineService = airlineService;
        this.auditService = auditService;
    }

    @GetMapping
    public List<AirlineDTO> getAll() {
        return airlineService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirlineDTO> getById(@PathVariable UUID id) {
        return airlineService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AirlineDTO> create(@Valid @RequestBody AirlineDTO dto,
                                              @AuthenticationPrincipal UserPrincipal principal,
                                              HttpServletRequest request) {
        AirlineDTO created = airlineService.create(dto);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "CREATE", "AIRLINE", created.getId().toString(),
                    "{\"code\":\"" + safe(created.getCode()) + "\",\"name\":\"" + safe(created.getName()) + "\"}",
                    request.getRemoteAddr());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirlineDTO> update(@PathVariable UUID id, @Valid @RequestBody AirlineDTO dto,
                                              @AuthenticationPrincipal UserPrincipal principal,
                                              HttpServletRequest request) {
        return airlineService.update(id, dto)
                .map(updated -> {
                    if (principal != null) {
                        auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                                "UPDATE", "AIRLINE", id.toString(),
                                "{\"code\":\"" + safe(updated.getCode()) + "\",\"name\":\"" + safe(updated.getName()) + "\"}",
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
        boolean removed = airlineService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "DELETE", "AIRLINE", id.toString(), "{}", request.getRemoteAddr());
        }
        return ResponseEntity.noContent().build();
    }

    private String safe(String v) {
        return v != null ? v.replace("\"", "'") : "";
    }
}
