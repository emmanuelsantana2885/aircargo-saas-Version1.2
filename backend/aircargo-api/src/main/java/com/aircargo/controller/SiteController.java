package com.aircargo.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.dto.SiteDTO;
import com.aircargo.service.AuditService;
import com.aircargo.service.SiteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    private final SiteService siteService;
    private final AuditService auditService;

    public SiteController(SiteService siteService, AuditService auditService) {
        this.siteService = siteService;
        this.auditService = auditService;
    }

    @GetMapping
    public List<SiteDTO> getAll() {
        return siteService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteDTO> getById(@PathVariable UUID id) {
        return siteService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SiteDTO> create(@Valid @RequestBody SiteDTO dto,
                                           @AuthenticationPrincipal UserPrincipal principal,
                                           HttpServletRequest request) {
        SiteDTO created = siteService.create(dto);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "CREATE", "SITE", created.getId().toString(),
                    "{\"code\":\"" + dto.getCode() + "\",\"name\":\"" + dto.getName() + "\"}",
                    request.getRemoteAddr());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteDTO> update(@PathVariable UUID id, @Valid @RequestBody SiteDTO dto,
                                           @AuthenticationPrincipal UserPrincipal principal,
                                           HttpServletRequest request) {
        return siteService.update(id, dto)
                .map(updated -> {
                    if (principal != null) {
                        auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                                "UPDATE", "SITE", id.toString(),
                                "{\"code\":\"" + dto.getCode() + "\",\"name\":\"" + dto.getName() + "\"}",
                                request.getRemoteAddr());
                    }
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal UserPrincipal principal,
                                       HttpServletRequest request) {
        boolean removed = siteService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "DELETE", "SITE", id.toString(), null, request.getRemoteAddr());
        }
        return ResponseEntity.noContent().build();
    }
}
