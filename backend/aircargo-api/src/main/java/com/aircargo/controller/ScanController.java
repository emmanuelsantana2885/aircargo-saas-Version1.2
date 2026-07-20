package com.aircargo.controller;

import com.aircargo.dto.ScanLookupDTO;
import com.aircargo.dto.ScanPieceRequest;
import com.aircargo.dto.ScanPieceResult;
import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.service.AuditService;
import com.aircargo.service.ScanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/scan")
public class ScanController {

    private final ScanService scanService;
    private final AuditService auditService;

    public ScanController(ScanService scanService, AuditService auditService) {
        this.scanService = scanService;
        this.auditService = auditService;
    }

    @GetMapping("/lookup")
    public ResponseEntity<?> lookup(@RequestParam String code,
                                    @RequestParam(required = false) UUID uldId) {
        ScanLookupDTO result = scanService.lookup(code, uldId);
        if (result == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Código no reconocido: " + code
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
                    "error", "No hay piezas para deshacer"
            ));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Última pieza eliminada"));
    }
}
