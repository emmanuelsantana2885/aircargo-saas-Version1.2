package com.aircargo.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.dto.TransferRequest;
import com.aircargo.dto.UldDTO;
import com.aircargo.entity.UldStatus;
import com.aircargo.service.AuditService;
import com.aircargo.service.UldService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ulds")
public class UldController {

    private final UldService uldService;
    private final AuditService auditService;

    public UldController(UldService uldService, AuditService auditService) {
        this.uldService = uldService;
        this.auditService = auditService;
    }

    @GetMapping
    public List<UldDTO> getAll(
            @RequestParam(required = false) UUID airlineId,
            @RequestParam(required = false) UUID flightId) {
        return uldService.getAll(airlineId, flightId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UldDTO> getById(@PathVariable UUID id) {
        return uldService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UldDTO> create(@Valid @RequestBody UldDTO dto,
                                          @AuthenticationPrincipal UserPrincipal principal,
                                          HttpServletRequest request) {
        UldDTO created = uldService.create(dto);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "CREATE", "ULD", created.getId().toString(),
                    "{\"uldNumber\":\"" + safe(created.getUldNumber()) + "\"}",
                    request.getRemoteAddr());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UldDTO> update(@PathVariable UUID id, @Valid @RequestBody UldDTO dto,
                                          @AuthenticationPrincipal UserPrincipal principal,
                                          HttpServletRequest request) {
        return uldService.update(id, dto)
                .map(updated -> {
                    if (principal != null) {
                        auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                                "UPDATE", "ULD", id.toString(),
                                "{\"uldNumber\":\"" + safe(updated.getUldNumber()) + "\"}",
                                request.getRemoteAddr());
                    }
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/flight")
    public ResponseEntity<?> assignFlight(@PathVariable UUID id, @RequestBody Map<String, String> body,
                                           @AuthenticationPrincipal UserPrincipal principal,
                                           HttpServletRequest request) {
        String flightIdStr = body.get("flightId");
        if (flightIdStr == null || flightIdStr.isBlank() || "null".equalsIgnoreCase(flightIdStr)) {
            try {
                UldDTO updated = uldService.assignFlight(id, null);
                if (principal != null) {
                    auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                            "UPDATE", "ULD", id.toString(),
                            "{\"action\":\"unassignFlight\"}",
                            request.getRemoteAddr());
                }
                return ResponseEntity.ok(updated);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getClass().getSimpleName() + ": " + e.getMessage()));
            }
        }
        try {
            UUID flightId = UUID.fromString(flightIdStr);
            UldDTO updated = uldService.assignFlight(id, flightId);
            if (principal != null) {
                auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                        "UPDATE", "ULD", id.toString(),
                        "{\"action\":\"assignFlight\",\"flightId\":\"" + flightId + "\"}",
                        request.getRemoteAddr());
            }
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    @PostMapping("/{uldId}/transfer")
    public ResponseEntity<?> transferUld(@PathVariable UUID uldId, @Valid @RequestBody TransferRequest transferRequest,
                                          @AuthenticationPrincipal UserPrincipal principal,
                                          HttpServletRequest request) {
        if (transferRequest.getDestinationFlightId() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "destinationFlightId is required"));
        }
        if (transferRequest.getReason() == null || transferRequest.getReason().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "reason is required for transfer"));
        }
        try {
            UldDTO result = uldService.transferUld(uldId, transferRequest.getDestinationFlightId(), transferRequest.getReason());
            if (principal != null) {
                auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                        "UPDATE", "ULD", uldId.toString(),
                        "{\"action\":\"transfer\",\"destinationFlightId\":\"" + transferRequest.getDestinationFlightId() + "\",\"reason\":\"" + safe(transferRequest.getReason()) + "\"}",
                        request.getRemoteAddr());
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getClass().getSimpleName() + ": " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal,
                                        HttpServletRequest request) {
        boolean removed = uldService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "DELETE", "ULD", id.toString(), null, request.getRemoteAddr());
        }
        return ResponseEntity.noContent().build();
    }

    private static String safe(String s) {
        return com.aircargo.common.util.TextUtil.safe(s);
    }
}
