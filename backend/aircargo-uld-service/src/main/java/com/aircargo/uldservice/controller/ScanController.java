package com.aircargo.uldservice.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.uldservice.config.ScanEventListener;
import com.aircargo.uldservice.dto.ScanLookupDTO;
import com.aircargo.uldservice.dto.ScanPieceRequest;
import com.aircargo.uldservice.dto.ScanPieceResult;
import com.aircargo.uldservice.repository.UldRepository;
import com.aircargo.uldservice.service.ScanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/scan")
public class ScanController {

    private final ScanService scanService;
    private final ScanEventListener scanEventListener;
    private final UldRepository uldRepository;
    private final ObjectMapper objectMapper;

    public ScanController(ScanService scanService,
                          ScanEventListener scanEventListener,
                          UldRepository uldRepository,
                          ObjectMapper objectMapper) {
        this.scanService = scanService;
        this.scanEventListener = scanEventListener;
        this.uldRepository = uldRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping(value = "/events/{flightId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable UUID flightId) {
        return scanEventListener.register(flightId);
    }

    @GetMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestParam String code,
                                    @RequestParam(required = false) UUID uldId) {
        ScanLookupDTO result = scanService.lookup(code, uldId);
        if (result == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Código no reconocido: " + code));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/piece")
    public ResponseEntity<?> registerPiece(@Valid @RequestBody ScanPieceRequest request,
                                           @AuthenticationPrincipal UserPrincipal principal,
                                           HttpServletRequest httpRequest) {
        try {
            UUID scannedBy = principal != null ? principal.getUserIdAsUuid() : null;
            ScanPieceResult result = scanService.registerPiece(request, scannedBy);

            if (result.isSuccess()) {
                uldRepository.findById(request.getUldId()).ifPresent(uld -> {
                    if (uld.getFlightId() != null) {
                        try {
                            scanEventListener.broadcastScanEvent(
                                    uld.getFlightId(),
                                    "piece-scanned",
                                    objectMapper.writeValueAsString(result));
                        } catch (Exception ignored) {}
                    }
                });
            }

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping("/piece/last")
    public ResponseEntity<?> undoLastPiece(@RequestParam UUID uldId,
                                           @RequestParam UUID mawbId) {
        boolean removed = scanService.undoLastPiece(uldId, mawbId);
        if (!removed) {
            return ResponseEntity.badRequest().body(Map.of("error", "No hay piezas para deshacer"));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Última pieza removida"));
    }
}
