package com.aircargo.mawbservice.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.common.dto.LabelPrintRequest;
import com.aircargo.common.audit.AuditService;
import com.aircargo.mawbservice.service.MawbLabelService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/mawbs/labels")
public class MawbLabelController {

    private final MawbLabelService mawbLabelService;
    private final AuditService auditService;

    public MawbLabelController(MawbLabelService mawbLabelService, AuditService auditService) {
        this.mawbLabelService = mawbLabelService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<?> generate(@RequestBody LabelPrintRequest request,
                                      @AuthenticationPrincipal UserPrincipal principal,
                                      HttpServletRequest httpRequest) {
        try {
            boolean zpl = "ZPL".equalsIgnoreCase(request.getFormat());
            if (zpl) {
                String zplData = mawbLabelService.renderZpl(request);
                audit(principal, httpRequest, "PRINT_CARGO_LABELS",
                        request.getTemplateId() != null ? request.getTemplateId().toString() : "default",
                        "{\"count\":" + (request.getIds() != null ? request.getIds().size() : 0) + ",\"format\":\"ZPL\"}");
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=MAWB_LABELS.zpl")
                        .contentType(MediaType.parseMediaType("application/octet-stream"))
                        .body(zplData.getBytes(StandardCharsets.UTF_8));
            }
            byte[] pdf = mawbLabelService.renderPdf(request);
            audit(principal, httpRequest, "PRINT_CARGO_LABELS",
                    request.getTemplateId() != null ? request.getTemplateId().toString() : "default",
                    "{\"count\":" + (request.getIds() != null ? request.getIds().size() : 0) + ",\"format\":\"PDF\"}");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=MAWB_LABELS.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", ex.getMessage()));
        }
    }

    private void audit(UserPrincipal principal, HttpServletRequest request, String action, String entityId, String details) {
        auditService.log(
                principal != null ? principal.getUserIdAsUuid() : null,
                principal != null ? principal.email() : "system",
                principal != null ? principal.fullName() : "system",
                action, "LABEL", entityId, details, request.getRemoteAddr()
        );
    }
}
