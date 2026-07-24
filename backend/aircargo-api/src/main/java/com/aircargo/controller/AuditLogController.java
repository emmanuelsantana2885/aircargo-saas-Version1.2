package com.aircargo.controller;

import com.aircargo.dto.AuditLogDTO;
import com.aircargo.dto.PageResponse;
import com.aircargo.entity.AuditLog;
import com.aircargo.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogRepository repository;

    public AuditLogController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AuditLogDTO> getAll(@RequestParam(required = false) UUID userId) {
        if (userId != null) {
            return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                    .map(AuditLogDTO::fromEntity)
                    .collect(Collectors.toList());
        }
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(AuditLogDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping(params = {"page", "size"})
    public PageResponse<AuditLogDTO> getAllPaginated(
            @RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageReq = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> result;
        if (userId != null) {
            result = repository.findByUserId(userId, pageReq);
        } else {
            result = repository.findAll(pageReq);
        }
        List<AuditLogDTO> dtos = result.getContent().stream()
                .map(AuditLogDTO::fromEntity)
                .collect(Collectors.toList());
        return PageResponse.of(dtos, page, size, result.getTotalElements());
    }
}
