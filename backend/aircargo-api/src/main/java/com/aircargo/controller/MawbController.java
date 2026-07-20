package com.aircargo.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.dto.MawbDTO;
import com.aircargo.entity.Flight;
import com.aircargo.entity.Hawb;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.repository.FlightRepository;
import com.aircargo.repository.HawbRepository;
import com.aircargo.repository.MawbRepository;
import com.aircargo.repository.WarehouseReceiptRepository;
import com.aircargo.service.AuditService;
import com.aircargo.service.MawbValidationService;
import com.aircargo.service.PdfGenerationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/cargo/mawbs")
public class MawbController {

    private final MawbRepository mawbRepository;
    private final HawbRepository hawbRepository;
    private final WarehouseReceiptRepository receiptRepository;
    private final FlightRepository flightRepository;
    private final MawbValidationService validationService;
    private final PdfGenerationService pdfService;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public MawbController(MawbRepository mawbRepository,
                          HawbRepository hawbRepository,
                          WarehouseReceiptRepository receiptRepository,
                          FlightRepository flightRepository,
                          MawbValidationService validationService,
                          PdfGenerationService pdfService,
                          ObjectMapper objectMapper,
                          AuditService auditService) {
        this.mawbRepository = mawbRepository;
        this.hawbRepository = hawbRepository;
        this.receiptRepository = receiptRepository;
        this.flightRepository = flightRepository;
        this.validationService = validationService;
        this.pdfService = pdfService;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<MawbDTO>> getAllMawbs() {
        List<MawbDTO> dtos = mawbRepository.findAll().stream()
                .map(MawbDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<MawbDTO>> getMawbsByFlight(@PathVariable UUID flightId) {
        List<MawbDTO> dtos = mawbRepository.findByFlightId(flightId).stream()
                .map(MawbDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<?> createMawb(@RequestBody Mawb mawb,
                                         @AuthenticationPrincipal UserPrincipal principal,
                                         HttpServletRequest request) {
        try {
            if (mawb.getAwbNumber() == null || mawb.getAwbNumber().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "awbNumber es requerido"));
            }
            validationService.validateAwbFormat(mawb.getAwbNumber());

            // Auto-resolve airline from Flight if not provided
            if (mawb.getAirline() == null && mawb.getFlight() != null && mawb.getFlight().getId() != null) {
                flightRepository.findById(mawb.getFlight().getId()).ifPresent(f -> mawb.setAirline(f.getAirline()));
            }
            if (mawb.getAirline() == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "airline es requerido (o proveer flightId para auto-resolver)"));
            }

            String weightWarning = null;
            if (mawb.getFlight() != null && mawb.getFlight().getId() != null && mawb.getChargeableWeightKg() != null) {
                weightWarning = validationService
                        .validateFlightPayloadLimit(mawb.getFlight().getId(), mawb.getChargeableWeightKg())
                        .orElse(null);
            }
            UUID airlineId = mawb.getAirline() != null ? mawb.getAirline().getId() : null;
            if (airlineId != null) {
                Optional<Mawb> existing = mawbRepository.findByAirlineIdAndAwbNumber(airlineId, mawb.getAwbNumber());
                if (existing.isPresent()) {
                    Mawb existingMawb = existing.get();
                    List<Hawb> hawbs = hawbRepository.findByMawbId(existingMawb.getId());
                    if (hawbs.size() < 2) {
                        return ResponseEntity.ok(existingMawb);
                    }
                }
            }
            Mawb savedMawb = mawbRepository.save(mawb);
            if (principal != null) {
                auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                        "CREATE", "MAWB", savedMawb.getId().toString(),
                        "{\"awbNumber\":\"" + safe(savedMawb.getAwbNumber()) + "\"}",
                        request.getRemoteAddr());
            }
            if (weightWarning != null) {
                Map<String, Object> body = new HashMap<>();
                body.put("mawb", savedMawb);
                body.put("weightWarning", weightWarning);
                return ResponseEntity.ok(body);
            }
            return ResponseEntity.ok(savedMawb);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(422).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error procesando el registro: " + ex.getMessage()));
        }
    }

    @PutMapping("/{mawbId}")
    public ResponseEntity<?> updateMawb(@PathVariable UUID mawbId, @RequestBody Mawb updates,
                                         @AuthenticationPrincipal UserPrincipal principal,
                                         HttpServletRequest request) {
        return mawbRepository.findById(mawbId).map(mawb -> {
            if (updates.getAirline() == null && updates.getFlight() != null && updates.getFlight().getId() != null) {
                flightRepository.findById(updates.getFlight().getId()).ifPresent(f -> updates.setAirline(f.getAirline()));
            }
            if (updates.getShipperName() != null) mawb.setShipperName(updates.getShipperName());
            if (updates.getConsigneeName() != null) mawb.setConsigneeName(updates.getConsigneeName());
            if (updates.getOrigin() != null) mawb.setOrigin(updates.getOrigin());
            if (updates.getDestination() != null) mawb.setDestination(updates.getDestination());
            if (updates.getPieces() != null) mawb.setPieces(updates.getPieces());
            if (updates.getReportedWeightKg() != null) mawb.setReportedWeightKg(updates.getReportedWeightKg());
            Mawb saved = mawbRepository.save(mawb);
            if (principal != null) {
                auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                        "UPDATE", "MAWB", mawbId.toString(),
                        "{\"awbNumber\":\"" + safe(saved.getAwbNumber()) + "\"}",
                        request.getRemoteAddr());
            }
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{mawbId}/status")
    public ResponseEntity<?> updateMawbStatus(@PathVariable UUID mawbId, @RequestBody Map<String, String> body,
                                               @AuthenticationPrincipal UserPrincipal principal,
                                               HttpServletRequest request) {
        return mawbRepository.findById(mawbId).map(mawb -> {
            String newStatus = body.get("status");
            if (newStatus == null || newStatus.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Se requiere el campo 'status'."));
            }
            try {
                MawbStatus status = MawbStatus.valueOf(newStatus.toUpperCase());
                mawb.setStatus(status);
                mawbRepository.save(mawb);
                if (principal != null) {
                    auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                            "UPDATE", "MAWB", mawbId.toString(),
                            "{\"status\":\"" + status.name() + "\"}",
                            request.getRemoteAddr());
                }
                return ResponseEntity.ok(Map.of("success", true, "status", status.name()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Estado inválido: " + newStatus));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene los documentos de soporte asociados a una MAWB (evidencia directa + de todos sus recibos).
     */
    @GetMapping("/{mawbId}/supporting-docs")
    public ResponseEntity<?> getSupportingDocs(@PathVariable UUID mawbId) {
        return mawbRepository.findById(mawbId).map(mawb -> {
            try {
                List<Map<String, String>> allDocs = new ArrayList<>();
                // 1. MAWB-level docs
                String mawbDocsRaw = mawb.getSupportingDocs();
                if (mawbDocsRaw != null && !mawbDocsRaw.isBlank() && !"[]".equals(mawbDocsRaw)) {
                    allDocs.addAll(objectMapper.readValue(mawbDocsRaw, new TypeReference<List<Map<String, String>>>() {}));
                }
                // 2. Receipt-level docs
                List<WarehouseReceipt> receipts = receiptRepository.findByMawbId(mawbId);
                for (WarehouseReceipt r : receipts) {
                    String raw = r.getSupportingDocs();
                    if (raw != null && !raw.isBlank() && !"[]".equals(raw)) {
                        List<Map<String, String>> receiptDocs = objectMapper.readValue(raw, new TypeReference<List<Map<String, String>>>() {});
                        for (Map<String, String> doc : receiptDocs) {
                            doc.put("_receiptId", r.getId().toString());
                            doc.put("_receiptDate", r.getReceiptDate() != null ? r.getReceiptDate().toString() : "");
                        }
                        allDocs.addAll(receiptDocs);
                    }
                }
                return ResponseEntity.ok(allDocs);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error leyendo documentos: " + e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza los documentos de soporte directos de una MAWB.
     */
    @PutMapping("/{mawbId}/supporting-docs")
    public ResponseEntity<?> updateSupportingDocs(@PathVariable UUID mawbId, @RequestBody List<Map<String, String>> docs,
                                                   @AuthenticationPrincipal UserPrincipal principal,
                                                   HttpServletRequest request) {
        return mawbRepository.findById(mawbId).map(mawb -> {
            try {
                mawb.setSupportingDocs(objectMapper.writeValueAsString(docs));
                mawbRepository.save(mawb);
                if (principal != null) {
                    auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                            "UPDATE", "MAWB", mawbId.toString(),
                            "{\"supportingDocsUpdated\":true,\"docCount\":" + docs.size() + "}",
                            request.getRemoteAddr());
                }
                return ResponseEntity.ok(Map.of("success", true));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error guardando documentos: " + e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Genera PDF con todas las evidencias documentales de una MAWB para auditoria.
     */
    @GetMapping("/{mawbId}/supporting-docs/pdf")
    public ResponseEntity<?> getSupportingDocsPdf(@PathVariable UUID mawbId) {
        return mawbRepository.findById(mawbId).map(mawb -> {
            try {
                List<Map<String, String>> allDocs = new ArrayList<>();
                String mawbDocsRaw = mawb.getSupportingDocs();
                if (mawbDocsRaw != null && !mawbDocsRaw.isBlank() && !"[]".equals(mawbDocsRaw)) {
                    allDocs.addAll(objectMapper.readValue(mawbDocsRaw, new TypeReference<List<Map<String, String>>>() {}));
                }
                List<WarehouseReceipt> receipts = receiptRepository.findByMawbId(mawbId);
                for (WarehouseReceipt r : receipts) {
                    String raw = r.getSupportingDocs();
                    if (raw != null && !raw.isBlank() && !"[]".equals(raw)) {
                        allDocs.addAll(objectMapper.readValue(raw, new TypeReference<List<Map<String, String>>>() {}));
                    }
                }

                String html = buildPdfHtml(mawb, allDocs, receipts);
                byte[] pdf = pdfService.generatePdf(html);

                String filename = "EVIDENCIAS_MAWB_" + (mawb.getAwbNumber() != null ? mawb.getAwbNumber().replace("/", "-") : mawbId.toString().substring(0, 8)) + ".pdf";
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdf);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error generando PDF: " + e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    private String buildPdfHtml(Mawb mawb, List<Map<String, String>> docs, List<WarehouseReceipt> receipts) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/>");
        sb.append("<style>");
        sb.append("body{font-family:Helvetica,Arial,sans-serif;color:#1a1a1a;padding:30px;font-size:10pt}");
        sb.append("h1{font-size:18pt;text-transform:uppercase;letter-spacing:0.05em;border-bottom:2px solid #333;padding-bottom:8px;margin-bottom:20px}");
        sb.append("h2{font-size:13pt;margin:25px 0 10px 0;color:#333;border-bottom:1px solid #ccc;padding-bottom:4px}");
        sb.append(".meta{font-size:9pt;color:#555;margin-bottom:25px;line-height:1.6}");
        sb.append(".meta strong{color:#1a1a1a}");
        sb.append("table{width:100%;border-collapse:collapse}");
        sb.append("td{border:1px solid #ddd;padding:6px;vertical-align:top;text-align:center;width:33%}");
        sb.append("td img{display:block;margin:0 auto;max-width:100%;max-height:150px}");
        sb.append(".ph{font-size:8pt;color:#999;padding:0.5cm;text-align:center}");
        sb.append(".name{font-size:7pt;color:#555;padding:4px 6px;text-align:center}");
        sb.append(".empty{font-size:10pt;color:#999;text-align:center;padding:40px 0}");
        sb.append(".footer{font-size:7pt;color:#aaa;text-align:center;margin-top:30px;border-top:1px solid #ddd;padding-top:6px}");
        sb.append(".sig-section{margin:20px 0 30px 0}");
        sb.append(".sig-receipt{margin-bottom:20px;padding:12px;border:1px solid #ddd;border-radius:4px;background:#fafafa}");
        sb.append(".sig-receipt h3{font-size:10pt;margin:0 0 8px 0;color:#333}");
        sb.append(".sig-grid{display:flex;flex-wrap:wrap;gap:10px}");
        sb.append(".sig-card{flex:1;min-width:180px;border:1px solid #ccc;border-radius:3px;padding:8px;background:white}");
        sb.append(".sig-card h4{font-size:8pt;margin:0 0 4px 0;color:#333;border-bottom:1px solid #eee;padding-bottom:3px}");
        sb.append(".sig-field{font-size:7pt;color:#555;margin:2px 0}");
        sb.append(".sig-field .val{font-size:8pt;color:#1a1a1a;font-weight:bold}");
        sb.append(".sig-img{max-height:1cm;max-width:4cm;display:block;margin:3px 0;border:1px solid #eee;padding:2px;background:white}");
        sb.append("</style></head><body>");

        sb.append("<h1>Evidencias Documentales</h1>");
        sb.append("<div class='meta'>");
        sb.append("<strong>MAWB:</strong> ").append(xmlEscape(mawb.getAwbNumber() != null ? mawb.getAwbNumber() : "—")).append("<br/>");
        sb.append("<strong>Shipper:</strong> ").append(xmlEscape(mawb.getShipperName() != null ? mawb.getShipperName() : "—")).append("<br/>");
        sb.append("<strong>Consignee:</strong> ").append(xmlEscape(mawb.getConsigneeName() != null ? mawb.getConsigneeName() : "—")).append("<br/>");
        sb.append("<strong>Ruta:</strong> ").append(xmlEscape(mawb.getOrigin() != null ? mawb.getOrigin() : "—")).append(" &#8594; ").append(xmlEscape(mawb.getDestination() != null ? mawb.getDestination() : "—"));
        sb.append("</div>");

        // Signature Evidence section
        boolean hasSignatures = false;
        for (WarehouseReceipt r : receipts) {
            if (hasAnySignature(r)) { hasSignatures = true; break; }
        }
        if (hasSignatures) {
            sb.append("<h2>Firmas de los Recibos de Bodega</h2>");
            sb.append("<div class='sig-section'>");
            int ri = 0;
            for (WarehouseReceipt r : receipts) {
                if (!hasAnySignature(r)) continue;
                ri++;
                sb.append("<div class='sig-receipt'>");
                sb.append("<h3>Recibo #").append(ri).append(" &mdash; ").append(xmlEscape(r.getPrintName() != null ? r.getPrintName() : (r.getReceivedByName() != null ? r.getReceivedByName() : "Sin nombre"))).append("</h3>");
                sb.append("<div class='sig-grid'>");

                // Dock / Received by
                boolean hasDock = (r.getDockSignature() != null && !r.getDockSignature().isEmpty())
                        || (r.getReceivedBySigUrl() != null && !r.getReceivedBySigUrl().isEmpty());
                if (hasDock) {
                    sb.append("<div class='sig-card'>");
                    sb.append("<h4>Recibido por (Almac\u00e9n)</h4>");
                    sb.append("<div class='sig-field'>Nombre: <span class='val'>").append(xmlEscape(r.getPrintName() != null ? r.getPrintName() : (r.getReceivedByName() != null ? r.getReceivedByName() : "\u2014"))).append("</span></div>");
                    if (r.getDockSignature() != null && !r.getDockSignature().isEmpty()) {
                        sb.append("<img class='sig-img' src='").append(r.getDockSignature()).append("' alt='Firma Recibido' />");
                    } else if (r.getReceivedBySigUrl() != null && !r.getReceivedBySigUrl().isEmpty()) {
                        sb.append("<img class='sig-img' src='").append(r.getReceivedBySigUrl()).append("' alt='Firma Recibido' />");
                    }
                    sb.append("</div>");
                }

                // Delivered by
                if (r.getDeliveredBySigUrl() != null && !r.getDeliveredBySigUrl().isEmpty()) {
                    sb.append("<div class='sig-card'>");
                    sb.append("<h4>Entregado por (Transportista)</h4>");
                    sb.append("<div class='sig-field'>Nombre: <span class='val'>").append(xmlEscape(r.getDeliveredByName() != null ? r.getDeliveredByName() : "\u2014")).append("</span></div>");
                    if (r.getDeliveredByIdNum() != null && !r.getDeliveredByIdNum().isEmpty()) {
                        sb.append("<div class='sig-field'>ID / C\u00e9dula: <span class='val'>").append(xmlEscape(r.getDeliveredByIdNum())).append("</span></div>");
                    }
                    sb.append("<img class='sig-img' src='").append(r.getDeliveredBySigUrl()).append("' alt='Firma Entregado' />");
                    sb.append("</div>");
                }

                // Broker
                if (r.getBrokerSigUrl() != null && !r.getBrokerSigUrl().isEmpty()) {
                    sb.append("<div class='sig-card'>");
                    sb.append("<h4>Representante de Broker</h4>");
                    sb.append("<div class='sig-field'>Nombre: <span class='val'>").append(xmlEscape(r.getBrokerName() != null ? r.getBrokerName() : "\u2014")).append("</span></div>");
                    if (r.getBrokerIdNum() != null && !r.getBrokerIdNum().isEmpty()) {
                        sb.append("<div class='sig-field'>ID / C\u00e9dula: <span class='val'>").append(xmlEscape(r.getBrokerIdNum())).append("</span></div>");
                    }
                    sb.append("<img class='sig-img' src='").append(r.getBrokerSigUrl()).append("' alt='Firma Broker' />");
                    sb.append("</div>");
                }

                sb.append("</div>");
                sb.append("</div>");
            }
            sb.append("</div>");
        }

        // Documents section
        sb.append("<h2>Documentos de Evidencia</h2>");
        if (docs.isEmpty()) {
            sb.append("<div class='empty'>Sin evidencias documentales asociadas a esta MAWB</div>");
        } else {
            sb.append("<table><tr>");
            int col = 0;
            for (Map<String, String> doc : docs) {
                String name = xmlEscape(doc.getOrDefault("name", "Documento"));
                String type = doc.getOrDefault("type", "document");
                String url = doc.getOrDefault("url", "");
                if ("document".equals(type) && url != null && url.startsWith("data:application/pdf")) {
                    String base64Data = url.substring(url.indexOf(',') + 1);
                    List<String> pageImages = pdfService.pdfPagesToDataUris(base64Data);
                    if (!pageImages.isEmpty()) {
                        for (int pi = 0; pi < pageImages.size(); pi++) {
                            if (col > 0 && col % 3 == 0) sb.append("</tr><tr>");
                            sb.append("<td>");
                            sb.append("<img src='").append(pageImages.get(pi)).append("' alt='").append(name).append("' />");
                            sb.append("<div class='name'>").append(name).append(" (p\u00e1gina ").append(pi + 1).append(")").append("</div>");
                            sb.append("</td>");
                            col++;
                        }
                    } else {
                        if (col > 0 && col % 3 == 0) sb.append("</tr><tr>");
                        sb.append("<td>");
                        sb.append("<div class='ph'>&#128196; ").append(name).append("</div>");
                        sb.append("<div class='name'>").append(name).append("</div>");
                        sb.append("</td>");
                        col++;
                    }
                } else if ("image".equals(type) && url != null && !url.isEmpty()) {
                    if (col > 0 && col % 3 == 0) sb.append("</tr><tr>");
                    sb.append("<td>");
                    if (url.length() > 500000) {
                        sb.append("<div class='ph'>&#128196; ").append(name).append("</div>");
                    } else {
                        sb.append("<img src='").append(url).append("' alt='").append(name).append("' />");
                    }
                    sb.append("<div class='name'>").append(name).append("</div>");
                    sb.append("</td>");
                    col++;
                } else {
                    if (col > 0 && col % 3 == 0) sb.append("</tr><tr>");
                    sb.append("<td>");
                    sb.append("<div class='ph'>&#128196; ").append(name).append("</div>");
                    sb.append("<div class='name'>").append(name).append("</div>");
                    sb.append("</td>");
                    col++;
                }
            }
            sb.append("</tr></table>");
        }

        sb.append("<div class='footer'>AirCargo &#8212; Documento generado el ").append(java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 16)).append(" &#8212; Documento de evidencia para auditoria</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static boolean hasAnySignature(WarehouseReceipt r) {
        return (r.getDockSignature() != null && !r.getDockSignature().isEmpty())
                || (r.getReceivedBySigUrl() != null && !r.getReceivedBySigUrl().isEmpty())
                || (r.getDeliveredBySigUrl() != null && !r.getDeliveredBySigUrl().isEmpty())
                || (r.getBrokerSigUrl() != null && !r.getBrokerSigUrl().isEmpty());
    }

    private static String xmlEscape(String s) {
        return com.aircargo.common.util.TextUtil.xmlEscape(s);
    }

    private static String safe(String s) {
        return com.aircargo.common.util.TextUtil.safe(s);
    }
}
