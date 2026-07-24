package com.aircargo.controller;

import com.aircargo.config.ErrorMessages;
import com.aircargo.config.ScanEventListener;
import com.aircargo.dto.ScanLookupDTO;
import com.aircargo.dto.ScanPieceRequest;
import com.aircargo.dto.ScanPieceResult;
import com.aircargo.entity.Uld;
import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.repository.UldRepository;
import com.aircargo.service.AuditService;
import com.aircargo.service.ScanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/scan")
public class ScanController {

    private final ScanService scanService;
    private final AuditService auditService;
    private final ScanEventListener scanEventListener;
    private final UldRepository uldRepository;
    private final ObjectMapper objectMapper;

    public ScanController(ScanService scanService, AuditService auditService,
                          ScanEventListener scanEventListener, UldRepository uldRepository,
                          ObjectMapper objectMapper) {
        this.scanService = scanService;
        this.auditService = auditService;
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
            return ResponseEntity.badRequest().body(Map.of(
                    "error", String.format(ErrorMessages.CODE_NOT_RECOGNIZED, code)
            ));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/piece")
    public ResponseEntity<?> registerPiece(@Valid @RequestBody ScanPieceRequest request,
                                           @AuthenticationPrincipal UserPrincipal principal,
                                           HttpServletRequest httpRequest) {
        try {
            UUID scannedBy = null;
            String email = null;
            String fullName = null;
            if (principal != null) {
                scannedBy = principal.getUserIdAsUuid();
                email = principal.email();
                fullName = principal.fullName();
            }

            ScanPieceResult result = scanService.registerPiece(request, scannedBy);

            if (result.isSuccess() && email != null) {
                auditService.log(scannedBy, email, fullName,
                        "SCAN_PIECE", "ULD", request.getUldId().toString(),
                        "{\"awbNumber\":\"" + request.getAwbNumber() +
                        "\",\"pieceNumber\":" + result.getPieceNumber() + "}",
                        httpRequest.getRemoteAddr());

                uldRepository.findById(request.getUldId()).ifPresent(uld -> {
                    if (uld.getFlight() != null) {
                        try {
                            scanEventListener.broadcastScanEvent(
                                    uld.getFlight().getId(),
                                    "piece-scanned",
                                    objectMapper.writeValueAsString(result));
                        } catch (Exception ignored) {
                        }
                    }
                });
            }

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/piece/last")
    public ResponseEntity<?> undoLastPiece(@RequestParam UUID uldId,
                                           @RequestParam UUID mawbId) {
        boolean removed = scanService.undoLastPiece(uldId, mawbId);
        if (!removed) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ErrorMessages.NO_PIECES_TO_UNDO
            ));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", ErrorMessages.LAST_PIECE_REMOVED));
    }
}
