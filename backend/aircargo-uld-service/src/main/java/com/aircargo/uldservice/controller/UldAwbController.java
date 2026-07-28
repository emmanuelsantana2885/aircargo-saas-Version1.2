package com.aircargo.uldservice.controller;

import com.aircargo.uldservice.dto.UldAwbDTO;
import com.aircargo.uldservice.service.UldAwbService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/uld-awbs")
public class UldAwbController {

    private final UldAwbService uldAwbService;

    public UldAwbController(UldAwbService uldAwbService) {
        this.uldAwbService = uldAwbService;
    }

    @GetMapping
    public List<UldAwbDTO> getAll(
            @RequestParam(required = false) UUID uldId,
            @RequestParam(required = false) UUID mawbId) {
        return uldAwbService.getAll(uldId, mawbId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UldAwbDTO> getById(@PathVariable UUID id) {
        return uldAwbService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UldAwbDTO> create(@Valid @RequestBody UldAwbDTO dto) {
        UldAwbDTO created = uldAwbService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UldAwbDTO> update(@PathVariable UUID id, @Valid @RequestBody UldAwbDTO dto) {
        return uldAwbService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean removed = uldAwbService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
