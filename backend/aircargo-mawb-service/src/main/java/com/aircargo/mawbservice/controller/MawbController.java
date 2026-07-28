package com.aircargo.mawbservice.controller;

import com.aircargo.mawbservice.dto.MawbDTO;
import com.aircargo.mawbservice.entity.MawbStatus;
import com.aircargo.mawbservice.service.MawbService;
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
@RequestMapping("/api/cargo/mawbs")
public class MawbController {

    private final MawbService mawbService;
    private final com.aircargo.mawbservice.service.AuditService auditService;

    public MawbController(MawbService mawbService, com.aircargo.mawbservice.service.AuditService auditService) {
        this.mawbService = mawbService;
        this.auditService = auditService;
    }

    @GetMapping
    public List<MawbDTO> getAll(
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UUID flightId,
            @RequestParam(required = false) MawbStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return mawbService.getAll(airlineId, flightId, status, page, size);
    }

    @GetMapping("/flight/{flightId}")
    public List<MawbDTO> getByFlight(@PathVariable UUID flightId) {
        return mawbService.getByFlightId(flightId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MawbDTO> getById(@PathVariable UUID id) {
        return mawbService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MawbDTO> create(@Valid @RequestBody MawbDTO dto,
                                           @AuthenticationPrincipal UserPrincipal principal,
                                           HttpServletRequest request) {
        MawbDTO created = mawbService.create(dto);
        auditService.log(
                principal != null ? principal.getUserIdAsUuid() : null,
                principal != null ? principal.email() : "system",
                principal != null ? principal.fullName() : "system",
                "CREATE", "MAWB", created.getId().toString(),
                "{\"awbNumber\":\"" + created.getAwbNumber() + "\"}",
                request.getRemoteAddr()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MawbDTO> update(@PathVariable UUID id, @Valid @RequestBody MawbDTO dto,
                                           @AuthenticationPrincipal UserPrincipal principal,
                                           HttpServletRequest request) {
        return mawbService.update(id, dto)
                .map(updated -> {
                    auditService.log(
                            principal != null ? principal.getUserIdAsUuid() : null,
                            principal != null ? principal.email() : "system",
                            principal != null ? principal.fullName() : "system",
                            "UPDATE", "MAWB", id.toString(),
                            "{\"awbNumber\":\"" + updated.getAwbNumber() + "\"}",
                            request.getRemoteAddr()
                    );
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MawbDTO> updateStatus(@PathVariable UUID id, @RequestBody MawbStatus status,
                                                 @AuthenticationPrincipal UserPrincipal principal,
                                                 HttpServletRequest request) {
        return mawbService.updateStatus(id, status)
                .map(updated -> {
                    auditService.log(
                            principal != null ? principal.getUserIdAsUuid() : null,
                            principal != null ? principal.email() : "system",
                            principal != null ? principal.fullName() : "system",
                            "UPDATE_STATUS", "MAWB", id.toString(),
                            "{\"status\":\"" + status.name() + "\"}",
                            request.getRemoteAddr()
                    );
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal,
                                        HttpServletRequest request) {
        boolean deleted = mawbService.delete(id);
        if (!deleted) return ResponseEntity.notFound().build();
        auditService.log(
                principal != null ? principal.getUserIdAsUuid() : null,
                principal != null ? principal.email() : "system",
                principal != null ? principal.fullName() : "system",
                "DELETE", "MAWB", id.toString(), null, request.getRemoteAddr()
        );
        return ResponseEntity.noContent().build();
    }
}