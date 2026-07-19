package com.aircargo.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.dto.RolePermissionBulkUpdateRequest;
import com.aircargo.dto.RolePermissionDTO;
import com.aircargo.dto.ViewPermissionDTO;
import com.aircargo.service.AuditService;
import com.aircargo.service.RolePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/role-permissions")
public class RolePermissionController {

    private final RolePermissionService service;
    private final AuditService auditService;

    public RolePermissionController(RolePermissionService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping("/views")
    public List<ViewPermissionDTO> getAllViews() {
        return service.getAllViews();
    }

    @GetMapping
    public List<RolePermissionDTO> getAllRolesWithPermissions() {
        return service.getAllRolesWithPermissions();
    }

    @GetMapping("/{role}")
    public RolePermissionDTO getRolePermissions(@PathVariable String role) {
        return service.getRolePermissions(role);
    }

    @PutMapping("/{role}")
    public ResponseEntity<Void> updateRolePermissions(@PathVariable String role,
                                                       @RequestBody RolePermissionBulkUpdateRequest request,
                                                       @AuthenticationPrincipal UserPrincipal principal,
                                                       HttpServletRequest httpRequest) {
        service.updateRolePermissions(role, request);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "UPDATE", "ROLE_PERMISSION", role,
                    "{\"permissions\":" + (request.getPermissions() != null ?
                        request.getPermissions().toString() : "{}") + "}",
                    httpRequest.getRemoteAddr());
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/views")
    public ResponseEntity<ViewPermissionDTO> createView(@Valid @RequestBody ViewPermissionDTO dto,
                                                          @AuthenticationPrincipal UserPrincipal principal,
                                                          HttpServletRequest request) {
        ViewPermissionDTO created = service.createView(dto);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "CREATE", "VIEW_PERMISSION", created.getId().toString(),
                    "{\"code\":\"" + dto.getCode() + "\",\"name\":\"" + dto.getName() + "\"}",
                    request.getRemoteAddr());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/views/{id}")
    public ResponseEntity<ViewPermissionDTO> updateView(@PathVariable UUID id,
                                                          @Valid @RequestBody ViewPermissionDTO dto,
                                                          @AuthenticationPrincipal UserPrincipal principal,
                                                          HttpServletRequest request) {
        ViewPermissionDTO updated = service.updateView(id, dto);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "UPDATE", "VIEW_PERMISSION", id.toString(),
                    "{\"code\":\"" + dto.getCode() + "\",\"name\":\"" + dto.getName() + "\"}",
                    request.getRemoteAddr());
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/views/{id}")
    public ResponseEntity<Void> deleteView(@PathVariable UUID id,
                                            @AuthenticationPrincipal UserPrincipal principal,
                                            HttpServletRequest request) {
        service.deleteView(id);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "DELETE", "VIEW_PERMISSION", id.toString(), null,
                    request.getRemoteAddr());
        }
        return ResponseEntity.noContent().build();
    }
}
