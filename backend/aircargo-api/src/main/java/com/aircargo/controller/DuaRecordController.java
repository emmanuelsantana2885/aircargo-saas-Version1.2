package com.aircargo.controller;

import com.aircargo.common.auth.UserPrincipal;
import com.aircargo.dto.DuaRecordDTO;
import com.aircargo.service.AuditService;
import com.aircargo.service.DuaRecordService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/compliance")
public class DuaRecordController {

    private final DuaRecordService service;
    private final AuditService auditService;

    public DuaRecordController(DuaRecordService service, AuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping
    public List<DuaRecordDTO> getAll() {
        return service.getAll();
    }

    @GetMapping("/mawb/{mawbId}")
    public List<DuaRecordDTO> getByMawb(@PathVariable UUID mawbId) {
        return service.getByMawb(mawbId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DuaRecordDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<DuaRecordDTO> create(@Valid @RequestBody DuaRecordDTO dto,
                                                @AuthenticationPrincipal UserPrincipal principal,
                                                HttpServletRequest request) {
        DuaRecordDTO created = service.create(dto);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "CREATE", "DUA_RECORD", created.getId().toString(),
                    "{\"mawbId\":\"" + dto.getMawbId() + "\",\"dua\":\"" + dto.getDuaNumber() + "\"}",
                    request.getRemoteAddr());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DuaRecordDTO> update(@PathVariable UUID id, @Valid @RequestBody DuaRecordDTO dto,
                                                @AuthenticationPrincipal UserPrincipal principal,
                                                HttpServletRequest request) {
        DuaRecordDTO updated = service.update(id, dto);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "UPDATE", "DUA_RECORD", id.toString(),
                    "{\"dua\":\"" + dto.getDuaNumber() + "\",\"status\":\"" + dto.getStatus() + "\"}",
                    request.getRemoteAddr());
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal UserPrincipal principal,
                                       HttpServletRequest request) {
        service.delete(id);
        if (principal != null) {
            auditService.log(principal.getUserIdAsUuid(), principal.email(), principal.fullName(),
                    "DELETE", "DUA_RECORD", id.toString(), null, request.getRemoteAddr());
        }
        return ResponseEntity.noContent().build();
    }
}
