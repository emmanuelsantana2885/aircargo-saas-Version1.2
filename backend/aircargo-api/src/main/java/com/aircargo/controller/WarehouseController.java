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

import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private final MawbRepository mawbRepository;

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

    // ═══════════════════════════════════════════════════════════════════
    //  DTOs
    // ═══════════════════════════════════════════════════════════════════

    public static class ReceiptData {
        public UUID airlineId;
        public String hawbId;
        public String gatewayCfs;
        public String shipperName;
        public String consigneeName;
        public String origin;
        public String destination;
        public Integer awbReportedPieces;
        public java.math.BigDecimal mawbWeightGreatest;
        public java.math.BigDecimal dimFactorIntl;
        public java.math.BigDecimal dimFactorDom;
        public Integer pieceCount;
        public Boolean cashOnly;
        public Boolean bookedInAcoms;
        public Boolean docsProvided;
        public Boolean customsCompleted;
        public Boolean preBuilt;
        public String remarks;
        public String dockSignature;
        public String printName;
        public String deliveredByName;
        public String deliveredByIdNum;
        public String deliveredBySigUrl;
        public String receivedByName;
        public String receivedBySigUrl;
        public String brokerName;
        public String brokerIdNum;
        public String brokerSigUrl;
        public String startDatetime;
        public String receiptDate;
    }

    public static class ReceiptPayload {
        public ReceiptData receipt;
        public List<ReceiptPieceData> pieces;
        public List<SupportingDoc> supportingDocs;
        public UUID mawbId;
        public String correctionReason;
    }

    public static class ReceiptPieceData {
        public Integer pieceNumber;
        public Integer pieces;
        public String hawbId;
        public java.math.BigDecimal lengthIn;
        public java.math.BigDecimal widthIn;
        public java.math.BigDecimal heightIn;
        public java.math.BigDecimal scaleWeightLbs;
        public java.math.BigDecimal scaleWeightKg;
        public java.math.BigDecimal dimWeightLbs;
        public java.math.BigDecimal dimWeightKg;
        public java.math.BigDecimal chargeableLbs;
        public java.math.BigDecimal chargeableKg;

        public ReceiptPiece toEntity() {
            ReceiptPiece p = new ReceiptPiece();
            p.setPieceNumber(pieceNumber);
            p.setPieces(pieces != null ? pieces : 1);
            UUID parsedHawbId = null;
            if (hawbId != null) {
                try { parsedHawbId = UUID.fromString(hawbId); } catch (IllegalArgumentException ignored) {}
            }
            p.setHawbId(parsedHawbId);
            p.setLengthIn(lengthIn != null ? lengthIn : java.math.BigDecimal.ZERO);
            p.setWidthIn(widthIn != null ? widthIn : java.math.BigDecimal.ZERO);
            p.setHeightIn(heightIn != null ? heightIn : java.math.BigDecimal.ZERO);
            p.setScaleWeightLbs(scaleWeightLbs != null ? scaleWeightLbs : java.math.BigDecimal.ZERO);
            p.setScaleWeightKg(scaleWeightKg != null ? scaleWeightKg : java.math.BigDecimal.ZERO);
            p.setDimWeightLbs(dimWeightLbs != null ? dimWeightLbs : java.math.BigDecimal.ZERO);
            p.setDimWeightKg(dimWeightKg != null ? dimWeightKg : java.math.BigDecimal.ZERO);
            p.setChargeableLbs(chargeableLbs != null ? chargeableLbs : java.math.BigDecimal.ZERO);
            p.setChargeableKg(chargeableKg != null ? chargeableKg : java.math.BigDecimal.ZERO);
            return p;
        }
    }

    public static class SupportingDoc {
        public String name;
        public String type;
        public String url;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Entity mapping
    // ═══════════════════════════════════════════════════════════════════

    private WarehouseReceipt toEntity(ReceiptData data, UUID mawbId) {
        WarehouseReceipt r = new WarehouseReceipt();
        r.setGatewayCfs(data.gatewayCfs != null ? data.gatewayCfs : "SDQ");
        r.setShipperName(data.shipperName);
        r.setConsigneeName(data.consigneeName);
        r.setOrigin(data.origin);
        r.setDestination(data.destination);
        r.setAwbReportedPieces(data.awbReportedPieces);
        r.setMawbWeightGreatest(data.mawbWeightGreatest);
        r.setDimFactorIntl(data.dimFactorIntl);
        r.setDimFactorDom(data.dimFactorDom);
        r.setPieceCount(data.pieceCount);
        r.setCashOnly(Boolean.TRUE.equals(data.cashOnly));
        r.setBookedInAcoms(Boolean.TRUE.equals(data.bookedInAcoms));
        r.setDocsProvided(Boolean.TRUE.equals(data.docsProvided));
        r.setCustomsCompleted(Boolean.TRUE.equals(data.customsCompleted));
        r.setPreBuilt(Boolean.TRUE.equals(data.preBuilt));
        r.setRemarks(data.remarks);
        r.setDockSignature(data.dockSignature);
        r.setPrintName(data.printName);
        r.setDeliveredByName(data.deliveredByName);
        r.setDeliveredByIdNum(data.deliveredByIdNum);
        r.setDeliveredBySigUrl(data.deliveredBySigUrl);
        r.setReceivedByName(data.receivedByName);
        r.setReceivedBySigUrl(data.receivedBySigUrl);
        r.setBrokerName(data.brokerName);
        r.setBrokerIdNum(data.brokerIdNum);
        r.setBrokerSigUrl(data.brokerSigUrl);
        UUID parsedHawbId = null;
        if (data.hawbId != null) {
            try { parsedHawbId = UUID.fromString(data.hawbId); } catch (IllegalArgumentException ignored) {}
        }
        r.setHawbId(parsedHawbId);
        if (data.airlineId != null) {
            com.aircargo.common.entity.Airline airline = new com.aircargo.common.entity.Airline();
            airline.setId(data.airlineId);
            r.setAirline(airline);
        }
        if (mawbId != null) {
            mawbRepository.findById(mawbId).ifPresent(r::setMawb);
        }
        if (data.startDatetime != null) {
            try { r.setStartDatetime(java.time.OffsetDateTime.parse(data.startDatetime)); } catch (Exception ignored) {}
        }
        if (data.receiptDate != null) {
            try { r.setReceiptDate(java.time.OffsetDateTime.parse(data.receiptDate)); } catch (Exception ignored) {}
        }
        return r;
    }

    private List<Map<String, String>> toDocsMap(List<SupportingDoc> docs) {
        if (docs == null) return null;
        return docs.stream()
            .map(d -> Map.of(
                "name", d.name != null ? d.name : "",
                "type", d.type != null ? d.type : "",
                "url", d.url != null ? d.url : ""
            ))
            .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  ENDPOINTS — Emit / Correct / Validate
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Emitir un nuevo recibo de bodega (primera emisión).
     * El backend agrupa piezas por hawbId y crea un receipt por cada HAWB.
     */
    @PostMapping("/emit")
    public ResponseEntity<?> emitWarehouseReceipt(@RequestBody ReceiptPayload payload,
                                                   @AuthenticationPrincipal UserPrincipal principal,
                                                   HttpServletRequest request) {
        try {
            if (payload.receipt == null || payload.pieces == null || payload.pieces.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", ErrorMessages.RECEIPT_DATA_INCOMPLETE));
            }

            WarehouseReceipt receipt = toEntity(payload.receipt, payload.mawbId);
            List<ReceiptPiece> pieces = payload.pieces.stream()
                    .map(ReceiptPieceData::toEntity)
                    .collect(Collectors.toList());
            List<Map<String, String>> docsMap = toDocsMap(payload.supportingDocs);

            List<WarehouseReceipt> created = warehouseService.createReceipt(receipt, pieces, docsMap);

            if (principal != null) {
                String mawbNum = receipt.getMawb() != null ? receipt.getMawb().getAwbNumber() : "";
                int totalPieces = payload.pieces.stream().mapToInt(p -> p.pieces != null ? p.pieces : 1).sum();
                auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                        "CREATE", "RECEIPT", created.isEmpty() ? "" : created.get(created.size() - 1).getId().toString(),
                        "{\"mawb\":\"" + safe(mawbNum) + "\",\"pieces\":" + totalPieces
                            + ",\"receipts\":" + created.size() + "}",
                        request.getRemoteAddr());
            }

            UUID lastId = created.isEmpty() ? null : created.get(created.size() - 1).getId();
            if (lastId != null) {
                asyncGenerateArtifacts(lastId);
            }
            final int totalPieces = payload.pieces.stream().mapToInt(p -> p.pieces != null ? p.pieces : 1).sum();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", lastId != null ? lastId.toString() : "",
                    "mawbId", payload.mawbId != null ? payload.mawbId.toString() : "",
                    "pieceCount", totalPieces,
                    "receiptCount", created.size()
            ));

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_PROCESS_ERROR, ex.getMessage())));
        }
    }

    /**
     * Actualizar un recibo existente in-place (edición directa).
     * Sobrescribe campos, piezas y evidencia sobre la misma entity.
     */
    @PutMapping("/{receiptId}")
    public ResponseEntity<?> updateReceipt(@PathVariable UUID receiptId, @RequestBody ReceiptPayload payload,
                                            @AuthenticationPrincipal UserPrincipal principal,
                                            HttpServletRequest request) {
        try {
            if (payload.receipt == null || payload.pieces == null || payload.pieces.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", ErrorMessages.RECEIPT_DATA_INCOMPLETE));
            }

            WarehouseReceipt updateEntity = toEntity(payload.receipt, payload.mawbId);
            List<ReceiptPiece> pieces = payload.pieces.stream()
                    .map(ReceiptPieceData::toEntity)
                    .collect(Collectors.toList());
            List<Map<String, String>> docsMap = toDocsMap(payload.supportingDocs);

            WarehouseReceipt saved = warehouseService.updateReceipt(receiptId, updateEntity, pieces, docsMap);

            if (principal != null) {
                String mawbNum = saved.getMawb() != null ? saved.getMawb().getAwbNumber() : "";
                int totalPieces = pieces.stream().mapToInt(p -> p.getPieces() != null ? p.getPieces() : 1).sum();
                auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                        "UPDATE", "RECEIPT", saved.getId().toString(),
                        "{\"mawb\":\"" + safe(mawbNum) + "\",\"pieces\":" + totalPieces + "}",
                        request.getRemoteAddr());
            }

            asyncGenerateArtifacts(saved.getId());

            final int totalPieces = payload.pieces.stream().mapToInt(p -> p.pieces != null ? p.pieces : 1).sum();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", saved.getId(),
                    "mawbId", payload.mawbId != null ? payload.mawbId.toString() : "",
                    "pieceCount", totalPieces
            ));

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_PROCESS_ERROR, ex.getMessage())));
        }
    }

    /**
     * Endpoint de validacion (dry-run): calcula si emitir el recibo provocaria
     * correcciones en el Booking (received > reserved) SIN guardar nada.
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateWarehouseReceipt(@RequestBody ReceiptPayload payload) {
        try {
            UUID mawbId = payload.mawbId;

            int newPieceCount = 0;
            if (payload.pieces != null) {
                newPieceCount = payload.pieces.stream()
                        .mapToInt(p -> p.pieces != null ? p.pieces : 1)
                        .sum();
            }

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

    // ═══════════════════════════════════════════════════════════════════
    //  ENDPOINTS — Read / Export
    // ═══════════════════════════════════════════════════════════════════

    @GetMapping("/{receiptId}/pieces")
    public ResponseEntity<List<ReceiptPiece>> getPiecesByReceipt(@PathVariable UUID receiptId) {
        return ResponseEntity.ok(pieceRepository.findByReceiptId(receiptId));
    }

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

    @GetMapping("/{receiptId}/export")
    public ResponseEntity<?> exportReceipt(@PathVariable UUID receiptId) {
        try {
            ByteArrayInputStream excel = exportService.exportReceipt(receiptId);
            WarehouseReceipt receipt = receiptRepository.findById(receiptId).orElse(null);
            String mawbNum = receipt != null && receipt.getMawb() != null
                    ? receipt.getMawb().getAwbNumber() : receiptId.toString().substring(0, 8);
            String versionTag = receiptVersionTag(receipt);
            String filename = "RECIBO_DE_BODEGA_AWB " + mawbNum + versionTag + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(excel));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", String.format(ErrorMessages.RECEIPT_EXPORT_ERROR, ex.getMessage())));
        }
    }

    @GetMapping("/{receiptId}/export-url")
    public ResponseEntity<?> getExportUrl(@PathVariable UUID receiptId) {
        try {
            WarehouseReceipt receipt = receiptRepository.findById(receiptId)
                    .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.RECEIPT_NOT_FOUND, receiptId)));
            String mawbNum = receipt.getMawb() != null
                    ? receipt.getMawb().getAwbNumber() : receiptId.toString().substring(0, 8);
            String versionTag = receiptVersionTag(receipt);
            String filename = "RECIBO_DE_BODEGA_AWB " + mawbNum + versionTag + ".xlsx";
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

    @GetMapping("/{receiptId}/pdf")
    public ResponseEntity<?> getReceiptPdf(@PathVariable UUID receiptId) {
        try {
            byte[] pdf = receiptFullPdfService.generateReceiptPdfFresh(receiptId);
            WarehouseReceipt receipt = receiptRepository.findById(receiptId).orElse(null);
            String mawbNum = receipt != null && receipt.getMawb() != null
                    ? receipt.getMawb().getAwbNumber() : receiptId.toString().substring(0, 8);
            String versionTag = receiptVersionTag(receipt);
            String filename = "RECIBO_DE_BODEGA_AWB " + mawbNum + versionTag + ".pdf";
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

    // ═══════════════════════════════════════════════════════════════════
    //  INTERNAL
    // ═══════════════════════════════════════════════════════════════════

    private void asyncGenerateArtifacts(UUID receiptId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    java.util.concurrent.CompletableFuture.runAsync(() -> generatePersistedArtifacts(receiptId));
                }
            });
        } else {
            java.util.concurrent.CompletableFuture.runAsync(() -> generatePersistedArtifacts(receiptId));
        }
    }

    private void generatePersistedArtifacts(UUID receiptId) {
        try {
            receiptFullPdfService.generateReceiptPdfFresh(receiptId);
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

    private static String receiptVersionTag(WarehouseReceipt receipt) {
        if (receipt == null) return "";
        int version = receipt.getCorrectionNumber() != null ? receipt.getCorrectionNumber() : 1;
        return "-V" + version;
    }
}
