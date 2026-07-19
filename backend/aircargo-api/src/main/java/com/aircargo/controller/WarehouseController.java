package com.aircargo.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.entity.ReceiptPiece;
import com.aircargo.repository.WarehouseReceiptRepository;
import com.aircargo.repository.ReceiptPieceRepository;
import com.aircargo.service.AuditService;
import com.aircargo.service.ReceiptExportService;
import com.aircargo.service.ReceiptFullPdfService;
import com.aircargo.service.WarehouseService;
import jakarta.servlet.http.HttpServletRequest;
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
                               AuditService auditService) {
        this.warehouseService = warehouseService;
        this.receiptRepository = receiptRepository;
        this.pieceRepository = pieceRepository;
        this.exportService = exportService;
        this.receiptFullPdfService = receiptFullPdfService;
        this.auditService = auditService;
    }

    /**
     * DTO interno temporal para recibir de golpe el encabezado y sus piezas en el payload JSON.
     */
    public static class ReceiptPayload {
        public WarehouseReceipt receipt;
        public List<ReceiptPiece> pieces;
        public List<SupportingDoc> supportingDocs;
        // Si es true, NO se purgan los demas recibos existentes del mismo MAWB antes de insertar este.
        // Se usa cuando se emiten varios recibos para el mismo MAWB en una sola sesion (recibo general
        // + un recibo por cada HAWB), para que cada llamada no borre la que la precedio.
        public Boolean appendOnly;
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
    public ResponseEntity<?> emitWarehouseReceipt(@RequestBody ReceiptPayload payload,
                                                   @AuthenticationPrincipal UserPrincipal principal,
                                                   HttpServletRequest request) {
        try {
            if (payload.receipt == null || payload.pieces == null || payload.pieces.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "DATOS INCOMPLETOS: El recibo debe contener al menos una pieza para cubicar."));
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
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error procesando el recibo en bodega: " + ex.getMessage()));
        }
    }

    /**
     * Endpoint para actualizar un recibo existente con nuevas piezas y evidencias (no acumulativo).
     */
    @PutMapping("/{receiptId}/emit")
    public ResponseEntity<?> updateWarehouseReceipt(@PathVariable UUID receiptId, @RequestBody ReceiptPayload payload,
                                                     @AuthenticationPrincipal UserPrincipal principal,
                                                     HttpServletRequest request) {
        try {
            if (payload.receipt == null || payload.pieces == null || payload.pieces.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "DATOS INCOMPLETOS: El recibo debe contener al menos una pieza para cubicar."));
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
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error actualizando recibo: " + ex.getMessage()));
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
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Error obteniendo evidencias: " + ex.getMessage()));
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
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Error generando documento de evidencias: " + ex.getMessage()));
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
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Error generando PDF de evidencias: " + ex.getMessage()));
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
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Error exportando recibo: " + ex.getMessage()));
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
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", "Error generando PDF del recibo: " + ex.getMessage()));
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
