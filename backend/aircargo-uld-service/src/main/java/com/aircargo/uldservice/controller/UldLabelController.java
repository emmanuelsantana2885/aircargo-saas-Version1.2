package com.aircargo.uldservice.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.uldservice.dto.LabelPrintRequest;
import com.aircargo.uldservice.service.PalletLabelService;
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
@RequestMapping("/api/ulds/labels")
public class UldLabelController {

    private final PalletLabelService palletLabelService;

    public UldLabelController(PalletLabelService palletLabelService) {
        this.palletLabelService = palletLabelService;
    }

    @PostMapping
    public ResponseEntity<?> generate(@RequestBody LabelPrintRequest request,
                                      @AuthenticationPrincipal UserPrincipal principal,
                                      HttpServletRequest httpRequest) {
        try {
            boolean zpl = "ZPL".equalsIgnoreCase(request.getFormat());
            if (zpl) {
                String zplData = palletLabelService.renderZpl(request);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PALLET_LABELS.zpl")
                        .contentType(MediaType.parseMediaType("application/octet-stream"))
                        .body(zplData.getBytes(StandardCharsets.UTF_8));
            }
            byte[] pdf = palletLabelService.renderPdf(request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PALLET_LABELS.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("success", false, "error", ex.getMessage()));
        }
    }
}
