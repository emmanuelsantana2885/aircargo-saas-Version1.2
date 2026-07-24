package com.aircargo.controller;

import com.aircargo.config.ErrorMessages;
import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.entity.ReceiptPiece;
import com.aircargo.repository.WarehouseReceiptRepository;
import com.aircargo.repository.ReceiptPieceRepository;
import com.aircargo.repository.MawbRepository;
import com.aircargo.service.AuditService;
import com.aircargo.service.ReceiptExportService;
import com.aircargo.service.ReceiptFullPdfService;
import com.aircargo.service.WarehouseService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/warehouse/receipts")
public class WarehouseController {

    private static final Logger log = LoggerFactory.getLogger(WarehouseController.class);

    private final WarehouseService warehouseService;
    private final WarehouseReceiptRepository receiptRepository;
    private final ReceiptPieceRepository pieceRepository;
    private final ReceiptExportService exportService;
    private final ReceiptFullPdfService receiptFullPdfService;
    private final AuditService auditService;

    public WarehouseController(WarehouseService warehouseService, 
                               WarehouseReceiptRepository receiptRepository, 
                               ReceiptPieceRepository pieceRepository,
                               ReceiptExportService exportService,
                               ReceiptFullPdfService receiptFullPdfService,
                               AuditService auditService,
                               MawbRepository mawbRepository) {
        this.warehouseService = warehouseService;
        this.receiptRepository = receiptRepository;
        this.pieceRepository = pieceRepository;
        this.exportService = exportService;
        this.receiptFullPdfService = receiptFullPdfService;
        this.auditService = auditService;
        this.mawbRepository = mawbRepository;
    }

    private final MawbRepository mawbRepository;

    /**
     * DTO interno temporal para recibir de golpe el encabezado y sus piezas en el payload JSON.
     */
    public static class ReceiptPayload {
        @NotNull(message = "El recibo es requerido")
        public WarehouseReceipt receipt;

        @NotNull(message = "Las piezas son requeridas")
        @NotEmpty(message = "Debe incluir al menos una pieza")
        public List<ReceiptPiece> pieces;

        public List<SupportingDoc> supportingDocs;
        public Boolean appendOnly;
        public UUID mawbId;
    }

    public static class SupportingDoc {
        public String name;
        public String type;
        public String url;
    }

    /**
     * Endpoint para emitir un nuevo recibo de bodega con cálculo en tiempo real de dimensiones.
     */
@PostMapping("/emit")
    public ResponseEntity<?> emitWarehouseReceipt(@Valid @RequestBody ReceiptPayload payload,
                                                   @AuthenticationPrincipal UserPrincipal principal,
                                                   HttpServletRequest request) {
        try {
            if (payload.receipt == null || payload.pieces == null || payload.pieces.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", ErrorMessages.RECEIPT_DATA_INCOMPLETE));
            }

            // Auto-resolve MAWB from mawbId if provided (frontend sends it at payload.mawbId)
            if (payload.receipt.getMawb() == null && payload.mawbId != null) {
                mawbRepository.findById(payload.mawbId).ifPresent(payload.receipt::setMawb);
            }

            List<Map<String, String>> docsMap = null;
            if (payload.supportingDocs != null) {
                docsMap = payload.supportingDocs.stream()
                    .map(d -> Map.of(
                        "name", d.name != null ? d.name : "",
                        "type", d.type != null ? d.type : "",
                        "url", d.url != null ? d.url : ""
                    ))
                    .collect(Collectors.toList());
            }
            WarehouseReceipt processed = warehouseService.processWarehouseReceipt(payload.receipt, payload.pieces, docsMap, !Boolean.TRUE.equals(payload.appendOnly));
            if (principal != null) {
                String mawbNum = processed.getMawb() != null ? processed.getMawb().getAwbNumber() : "";
                auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                        "CREATE", "RECEIPT", processed.getId().toString(),
                        "{\"mawb\":\"" + safe(mawbNum) + "\",\"pieces\":" + (payload.pieces != null ? payload.pieces.size() : 0) + "}",
                        request.getRemoteAddr());
            }
            final UUID receiptId = processed.getId();
            java.util.concurrent.CompletableFuture.runAsync(() -> generatePersistedArtifacts(receiptId));
            return ResponseEntity.ok(processed);

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_PROCESS_ERROR, ex.getMessage())));
        }
    }

    /**
     * Endpoint de validacion (dry-run): calcula si emitir el recibo provocaria
     * correcciones en el Booking (received > reserved) SIN guardar nada.
     * Retorna { valid: true, corrections: [...] }
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateWarehouseReceipt(@Valid @RequestBody ReceiptPayload payload) {
        try {
            if (payload.receipt == null || payload.pieces == null || payload.pieces.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", ErrorMessages.RECEIPT_DATA_INCOMPLETE_SHORT));
            }

            UUID mawbId = payload.mawbId;
            if (mawbId == null && payload.receipt.getMawb() != null) {
                mawbId = payload.receipt.getMawb().getId();
            }

            int newPieceCount = payload.pieces.stream()
                    .mapToInt(p -> p.getPieces() != null ? p.getPieces() : 1)
                    .sum();

            List<String> corrections = warehouseService.validateBookingCorrections(mawbId, newPieceCount);
            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "corrections", corrections,
                    "willCorrect", !corrections.isEmpty()
            ));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_VALIDATION_ERROR, ex.getMessage())));
        }
    }

    /**
     * Endpoint para actualizar un recibo existente con nuevas piezas y evidencias (no acumulativo).
     */
    @PutMapping("/{receiptId}/emit")
    public ResponseEntity<?> updateWarehouseReceipt(@PathVariable UUID receiptId, @Valid @RequestBody ReceiptPayload payload,
                                                     @AuthenticationPrincipal UserPrincipal principal,
                                                     HttpServletRequest request) {
        try {
            if (payload.receipt == null || payload.pieces == null || payload.pieces.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", ErrorMessages.RECEIPT_DATA_INCOMPLETE));
            }

            // Auto-resolve MAWB from mawbId if provided (frontend sends it at payload.mawbId)
            if (payload.receipt.getMawb() == null && payload.mawbId != null) {
                mawbRepository.findById(payload.mawbId).ifPresent(payload.receipt::setMawb);
            }

            List<Map<String, String>> docsMap = null;
            if (payload.supportingDocs != null) {
                docsMap = payload.supportingDocs.stream()
                    .map(d -> Map.of(
                        "name", d.name != null ? d.name : "",
                        "type", d.type != null ? d.type : "",
                        "url", d.url != null ? d.url : ""
                    ))
                    .collect(Collectors.toList());
            }
            WarehouseReceipt processed = warehouseService.updateWarehouseReceipt(receiptId, payload.receipt, payload.pieces, docsMap);
            if (principal != null) {
                String mawbNum = processed.getMawb() != null ? processed.getMawb().getAwbNumber() : "";
                auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                        "UPDATE", "RECEIPT", receiptId.toString(),
                        "{\"mawb\":\"" + safe(mawbNum) + "\",\"pieces\":" + (payload.pieces != null ? payload.pieces.size() : 0) + "}",
                        request.getRemoteAddr());
            }
            final UUID savedId = processed.getId();
            java.util.concurrent.CompletableFuture.runAsync(() -> generatePersistedArtifacts(savedId));
            return ResponseEntity.ok(processed);

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_UPDATE_ERROR, ex.getMessage())));
        }
    }

    /**
     * Endpoint para consultar el desglose de piezas cubicadas asociadas a un recibo.
     */
    @GetMapping("/{receiptId}/pieces")
    public ResponseEntity<List<ReceiptPiece>> getPiecesByReceipt(@PathVariable UUID receiptId) {
        return ResponseEntity.ok(pieceRepository.findByReceiptId(receiptId));
    }

    /**
     * Endpoint para consultar las evidencias documentales de un recibo en formato JSON.
     */
    @GetMapping("/{receiptId}/supporting-docs")
    public ResponseEntity<?> getSupportingDocsJson(@PathVariable UUID receiptId) {
        try {
            return receiptRepository.findById(receiptId)
                .map(r -> {
                    String raw = r.getSupportingDocs();
                    if (raw == null || raw.isBlank()) return ResponseEntity.ok(List.of());
                    try {
                        return ResponseEntity.ok(warehouseService.getObjectMapper().readValue(raw, List.class));
                    } catch (Exception e) {
                        return ResponseEntity.ok(List.of());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_EVIDENCE_JSON_ERROR, ex.getMessage())));
        }
    }

    /**
     * Endpoint para generar HTML con todas las evidencias documentales de un recibo.
     */
    @GetMapping("/{receiptId}/supporting-docs/html")
    public ResponseEntity<?> getSupportingDocsHtml(@PathVariable UUID receiptId) {
        try {
            String html = warehouseService.generateSupportingDocsHtml(receiptId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=EVIDENCIAS_" + receiptId.toString().substring(0, 8) + ".html")
                    .contentType(MediaType.parseMediaType("text/html; charset=UTF-8"))
                    .body(html);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_EVIDENCE_HTML_ERROR, ex.getMessage())));
        }
    }

    /**
     * Endpoint para generar PDF con todas las evidencias documentales de un recibo.
     */
    @GetMapping("/{receiptId}/supporting-docs/pdf")
    public ResponseEntity<?> getSupportingDocsPdf(@PathVariable UUID receiptId) {
        try {
            byte[] pdf = warehouseService.generateSupportingDocsPdf(receiptId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=EVIDENCIAS_" + receiptId.toString().substring(0, 8) + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_EVIDENCE_PDF_ERROR, ex.getMessage())));
        }
    }

    /**
     * Endpoint para exportar un recibo de bodega a Excel con desglose completo.
     */
    @GetMapping("/{receiptId}/export")
    public ResponseEntity<?> exportReceipt(@PathVariable UUID receiptId) {
        try {
            ByteArrayInputStream excel = exportService.exportReceipt(receiptId);
            WarehouseReceipt receipt = receiptRepository.findById(receiptId).orElse(null);
            String mawbNum = receipt != null && receipt.getMawb() != null
                    ? receipt.getMawb().getAwbNumber() : receiptId.toString().substring(0, 8);
            String filename = "RECIBO_DE_BODEGA_AWB " + mawbNum + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(excel));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_EXPORT_ERROR, ex.getMessage())));
        }
    }

    /**
     * Endpoint for mobile clients: returns a downloadable URL for the receipt Excel export.
     */
    @GetMapping("/{receiptId}/export-url")
    public ResponseEntity<?> getExportUrl(@PathVariable UUID receiptId) {
        try {
            WarehouseReceipt receipt = receiptRepository.findById(receiptId)
                    .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.RECEIPT_NOT_FOUND, receiptId)));
            String mawbNum = receipt.getMawb() != null
                    ? receipt.getMawb().getAwbNumber() : receiptId.toString().substring(0, 8);
            String filename = "RECIBO_DE_BODEGA_AWB " + mawbNum + ".xlsx";
            String encodedFilename = java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String url = "/api/warehouse/receipts/" + receiptId + "/export?filename=" + encodedFilename;
            return ResponseEntity.ok(Map.of(
                    "url", url,
                    "filename", filename,
                    "contentType", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_EXPORT_URL_ERROR, ex.getMessage())));
        }
    }

    /**
     * Endpoint para descargar el PDF completo del recibo de bodega (piezas + firmas + anexo HAWB).
     */
    @GetMapping("/{receiptId}/pdf")
    public ResponseEntity<?> getReceiptPdf(@PathVariable UUID receiptId) {
        try {
            byte[] pdf = receiptFullPdfService.generateReceiptPdf(receiptId);
            WarehouseReceipt receipt = receiptRepository.findById(receiptId).orElse(null);
            String mawbNum = receipt != null && receipt.getMawb() != null
                    ? receipt.getMawb().getAwbNumber() : receiptId.toString().substring(0, 8);
            String filename = "RECIBO_DE_BODEGA_AWB " + mawbNum + ".pdf";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_PDF_ERROR, ex.getMessage())));
        }
    }

    /**
     * Generates and persists PDF + XLSX artifacts for a receipt. Called AFTER the main
     * transaction commits so heavy rendering (openhtmltopdf / POI) does not block the DB.
     */
    private void generatePersistedArtifacts(UUID receiptId) {
        try {
            receiptFullPdfService.generateReceiptPdf(receiptId);
        } catch (Exception e) {
            log.warn("No se pudo generar PDF para recibo {}: {}", receiptId, e.getMessage());
        }
        try {
            exportService.generateAndPersistExcel(receiptId);
        } catch (Exception e) {
            log.warn("No se pudo generar Excel para recibo {}: {}", receiptId, e.getMessage());
        }
    }

    private static String safe(String s) {
        return com.aircargo.common.util.TextUtil.safe(s);
    }
}
