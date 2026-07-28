package com.aircargo.loadplanningservice.controller;

import com.aircargo.common.util.TextUtil;
import com.aircargo.feign.dto.UldAwbDTO;
import com.aircargo.feign.dto.UldDTO;
import com.aircargo.loadplanningservice.dto.LoadPlanningDTO;
import com.aircargo.loadplanningservice.dto.LoadPlanningUldDTO;
import com.aircargo.loadplanningservice.service.LoadPlanningExportService;
import com.aircargo.loadplanningservice.service.LoadPlanningService;
import com.aircargo.loadplanningservice.service.RampManifestParserService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/load-planning")
public class LoadPlanningController {

    private final LoadPlanningService loadPlanningService;
    private final RampManifestParserService manifestParserService;
    private final LoadPlanningExportService exportService;

    public LoadPlanningController(LoadPlanningService loadPlanningService,
                                   RampManifestParserService manifestParserService,
                                   LoadPlanningExportService exportService) {
        this.loadPlanningService = loadPlanningService;
        this.manifestParserService = manifestParserService;
        this.exportService = exportService;
    }

    @PostMapping("/flight/{flightId}/close")
    public ResponseEntity<?> closeLoadPlan(@PathVariable java.util.UUID flightId) {
        try {
            LoadPlanningDTO result = loadPlanningService.closeLoadPlan(flightId);
            return ResponseEntity.ok(result);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        }
    }

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<?> getLoadPlanningByFlight(@PathVariable java.util.UUID flightId) {
        return loadPlanningService.getByFlightId(flightId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/flight/{flightId}/upload-manifest")
    public ResponseEntity<?> uploadRampManifest(@PathVariable java.util.UUID flightId,
                                                 @RequestParam("airlineId") java.util.UUID airlineId,
                                                 @RequestParam("file") MultipartFile file) {
        try {
            List<UldDTO> uldsExtraidos = manifestParserService.parseExcelToNativeUld(file, flightId, airlineId);
            return ResponseEntity.ok().body(Map.of(
                "success", true,
                "message", String.format("Exito: Se inyectaron %d ULDs nativos al plan de vuelo.", uldsExtraidos.size())
            ));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        }
    }

    @GetMapping("/flight/{flightId}/export-manifest")
    public ResponseEntity<Resource> downloadRampManifest(@PathVariable java.util.UUID flightId) {
        try {
            ByteArrayInputStream in = exportService.exportFlightLoadPlan(flightId);
            InputStreamResource resource = new InputStreamResource(in);

            String filename = String.format("LOAD_PLAN_FLIGHT_%s.xlsx", flightId.toString().substring(0, 8));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/flight/{flightId}/pallet-sheets")
    public ResponseEntity<Resource> downloadPalletSheets(@PathVariable java.util.UUID flightId) {
        try {
            Optional<LoadPlanningDTO> opt = loadPlanningService.getByFlightId(flightId);
            if (opt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            LoadPlanningDTO plan = opt.get();
            String html = buildPalletSheetsHtml(plan);
            byte[] pdf = generatePdf(html);

            String flightNum = plan.getFlightNumber() != null ? plan.getFlightNumber().replaceAll("[^a-zA-Z0-9]", "_") : flightId.toString().substring(0, 8);
            String filename = String.format("PALLET_SHEETS_UPS-%s.pdf", flightNum);

            ByteArrayInputStream in = new ByteArrayInputStream(pdf);
            InputStreamResource resource = new InputStreamResource(in);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[] generatePdf(String html) throws Exception {
        try (java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream()) {
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        }
    }

    private String buildPalletSheetsHtml(LoadPlanningDTO plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'/><title>Pallet Sheets</title>");
        sb.append("<style>");
        sb.append("@page { margin: 12pt 16pt; size: letter portrait; }");
        sb.append("body { font-family: Tahoma, sans-serif; font-size: 9pt; color: #1e293b; margin: 0; padding: 0; }");
        sb.append(".page { page-break-after: always; padding: 0; }");
        sb.append("h2 { font-size: 11pt; margin: 0 0 4pt 0; text-align: center; text-transform: uppercase; letter-spacing: 1pt; }");
        sb.append(".flight-info { text-align: center; font-size: 8pt; margin-bottom: 6pt; color: #475569; }");
        sb.append(".uld-header { display: flex; justify-content: space-between; border: 1px solid #1e293b; padding: 4pt 6pt; margin-bottom: 6pt; font-size: 8pt; }");
        sb.append(".uld-header div { font-weight: bold; }");
        sb.append(".uld-header span { font-weight: normal; }");
        sb.append("table { width: 100%; border-collapse: collapse; font-size: 8pt; }");
        sb.append("th { background-color: #1e293b; color: white; padding: 4pt 3pt; text-align: left; font-size: 7.5pt; text-transform: uppercase; }");
        sb.append("td { padding: 3pt; border-bottom: 1px solid #cbd5e1; }");
        sb.append("tr:nth-child(even) td { background-color: #f8fafc; }");
        sb.append(".total-row td { font-weight: bold; border-top: 2px solid #1e293b; background-color: #e2e8f0 !important; }");
        sb.append(".footer { text-align: center; font-size: 7pt; color: #94a3b8; margin-top: 6pt; }");
        sb.append("</style></head><body>");

        List<LoadPlanningUldDTO> ulds = plan.getUlds();
        if (ulds == null || ulds.isEmpty()) {
            sb.append("<p>No hay ULDs asignados a este vuelo.</p>");
        } else {
            for (int i = 0; i < ulds.size(); i++) {
                LoadPlanningUldDTO uld = ulds.get(i);
                if (i < ulds.size() - 1) sb.append("<div class='page'>");
                else sb.append("<div>");

                sb.append("<h2>ULD Pallet Sheet &amp; Manifest</h2>");
                sb.append("<div class='flight-info'>");
                sb.append("UPS-").append(xmlEscape(plan.getFlightNumber() != null ? plan.getFlightNumber() : ""));
                sb.append(" &#160;|&#160; ").append(xmlEscape(plan.getOrigin() != null ? plan.getOrigin() : ""));
                sb.append(" &#8594; ").append(xmlEscape(plan.getDestination() != null ? plan.getDestination() : ""));
                if (plan.getFlightDate() != null) {
                    sb.append(" &#160;|&#160; ").append(plan.getFlightDate().toString());
                }
                if (plan.getAircraftReg() != null) {
                    sb.append(" &#160;|&#160; A/C: ").append(xmlEscape(plan.getAircraftReg()));
                }
                sb.append("</div>");

                sb.append("<div class='uld-header'>");
                sb.append("<div>ULD: <span>").append(xmlEscape(uld.getUldNumber() != null ? uld.getUldNumber() : "-")).append("</span></div>");
                sb.append("<div>Type: <span>").append(uld.getUldType() != null ? uld.getUldType() : "-").append("</span></div>");
                sb.append("<div>Pos: <span>").append(xmlEscape(uld.getPosition() != null ? uld.getPosition() : "-")).append("</span></div>");
                sb.append("<div>Seal: <span>").append(xmlEscape(uld.getSealNumber() != null ? uld.getSealNumber() : "-")).append("</span></div>");
                sb.append("<div>Status: <span>").append(uld.getStatus() != null ? uld.getStatus() : "OPEN").append("</span></div>");
                sb.append("</div>");

                List<UldAwbDTO> awbs = uld.getAwbs();
                sb.append("<table>");
                sb.append("<thead><tr>");
                sb.append("<th>#</th>");
                sb.append("<th>MAWB</th>");
                sb.append("<th style='text-align:right'>Pieces</th>");
                sb.append("<th>Destination</th>");
                sb.append("<th>Commodity</th>");
                sb.append("</tr></thead><tbody>");

                int totalPieces = 0;
                if (awbs != null && !awbs.isEmpty()) {
                    int idx = 1;
                    for (UldAwbDTO awb : awbs) {
                        int pcs = awb.getPieces() != null ? awb.getPieces() : 0;
                        totalPieces += pcs;
                        sb.append("<tr>");
                        sb.append("<td>").append(idx++).append("</td>");
                        sb.append("<td>").append(xmlEscape(awb.getMawbLabel() != null ? awb.getMawbLabel() : "-")).append("</td>");
                        sb.append("<td style='text-align:right'>").append(pcs).append("</td>");
                        sb.append("<td>").append(xmlEscape(awb.getDestination() != null ? awb.getDestination() : "-")).append("</td>");
                        sb.append("<td>").append(awb.getDescription() != null ? awb.getDescription() : "-").append("</td>");
                        sb.append("</tr>");
                    }
                } else {
                    sb.append("<tr><td colspan='5' style='text-align:center;color:#94a3b8;'>No MAWBs assigned</td></tr>");
                }

                sb.append("<tr class='total-row'><td colspan='2' style='text-align:right'>TOTAL</td>");
                sb.append("<td style='text-align:right'>").append(totalPieces).append(" pcs</td>");
                sb.append("<td colspan='2'>");
                sb.append("Gross: ").append(uld.getGrossWeightLbs() != null ? uld.getGrossWeightLbs().stripTrailingZeros().toPlainString() : "0").append(" lb");
                sb.append(" &#160; Tare: ").append(uld.getTareLbs() != null ? uld.getTareLbs().stripTrailingZeros().toPlainString() : "0").append(" lb");
                sb.append(" &#160; Net: ").append(uld.getNetWeightLbs() != null ? uld.getNetWeightLbs().stripTrailingZeros().toPlainString() : "0").append(" lb");
                sb.append("</td></tr>");

                sb.append("</tbody></table>");
                sb.append("<div class='footer'>SDQ Operations &#8212; UPS Airlines &#8212; Pallet Sheet #").append(i + 1).append(" of ").append(ulds.size()).append("</div>");
                sb.append("</div>");
            }
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    private String xmlEscape(String s) {
        return TextUtil.xmlEscape(s);
    }
}
