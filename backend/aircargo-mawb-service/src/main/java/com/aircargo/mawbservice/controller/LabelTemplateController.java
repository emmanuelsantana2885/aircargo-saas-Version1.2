package com.aircargo.mawbservice.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.mawbservice.dto.LabelTemplateDTO;
import com.aircargo.mawbservice.entity.LabelType;
import com.aircargo.mawbservice.service.AuditService;
import com.aircargo.mawbservice.service.LabelTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/label-templates")
public class LabelTemplateController {

    private final LabelTemplateService labelTemplateService;
    private final AuditService auditService;

    public LabelTemplateController(LabelTemplateService labelTemplateService, AuditService auditService) {
        this.labelTemplateService = labelTemplateService;
        this.auditService = auditService;
    }

    @GetMapping
    public List<LabelTemplateDTO> getAll(@RequestParam(required = false, defaultValue = "CARGO") LabelType type) {
        return labelTemplateService.getAll(type);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LabelTemplateDTO> getById(@PathVariable UUID id) {
        return labelTemplateService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LabelTemplateDTO> create(@Valid @RequestBody LabelTemplateDTO dto,
                                                    @AuthenticationPrincipal UserPrincipal principal,
                                                    HttpServletRequest request) {
        LabelTemplateDTO created = labelTemplateService.create(dto);
        audit(principal, request, "CREATE_LABEL_TEMPLATE", created.getId().toString(),
                "{\"name\":\"" + safe(created.getName()) + "\",\"type\":\"" + created.getType() + "\"}");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LabelTemplateDTO> update(@PathVariable UUID id,
                                                    @Valid @RequestBody LabelTemplateDTO dto,
                                                    @AuthenticationPrincipal UserPrincipal principal,
                                                    HttpServletRequest request) {
        return labelTemplateService.update(id, dto)
                .map(updated -> {
                    audit(principal, request, "UPDATE_LABEL_TEMPLATE", id.toString(),
                            "{\"name\":\"" + safe(updated.getName()) + "\",\"type\":\"" + updated.getType() + "\"}");
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                        @AuthenticationPrincipal UserPrincipal principal,
                                        HttpServletRequest request) {
        boolean deleted = labelTemplateService.delete(id);
        if (!deleted) return ResponseEntity.notFound().build();
        audit(principal, request, "DELETE_LABEL_TEMPLATE", id.toString(), null);
        return ResponseEntity.noContent().build();
    }

    private void audit(UserPrincipal principal, HttpServletRequest request, String action, String id, String details) {
        auditService.log(
                principal != null ? principal.getUserIdAsUuid() : null,
                principal != null ? principal.email() : "system",
                principal != null ? principal.fullName() : "system",
                action, "LABEL_TEMPLATE", id, details,
                request.getRemoteAddr()
        );
    }

    private static String safe(String s) {
        return com.aircargo.common.util.TextUtil.safe(s);
    }
}
