package com.aircargo.uldservice.controller;

import com.aircargo.uldservice.dto.UldTypeConfigDTO;
import com.aircargo.uldservice.service.UldTypeConfigService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/uld-type-config")
public class UldTypeConfigController {

    private final UldTypeConfigService service;

    public UldTypeConfigController(UldTypeConfigService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<UldTypeConfigDTO>> getAll(@RequestParam(required = false) UUID airlineId) {
        return ResponseEntity.ok(service.getAll(airlineId));
    }

    @GetMapping("/{airlineId}")
    public ResponseEntity<List<UldTypeConfigDTO>> getByAirline(@PathVariable UUID airlineId) {
        return ResponseEntity.ok(service.getAll(airlineId));
    }

    @GetMapping("/config/{id}")
    public ResponseEntity<UldTypeConfigDTO> getById(@PathVariable UUID id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UldTypeConfigDTO> create(@Valid @RequestBody UldTypeConfigDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UldTypeConfigDTO> update(@PathVariable UUID id, @Valid @RequestBody UldTypeConfigDTO dto) {
        return service.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return service.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/airline/{airlineId}/bulk")
    public ResponseEntity<List<UldTypeConfigDTO>> replaceAllForAirline(@PathVariable UUID airlineId,
                                                                       @RequestBody List<UldTypeConfigDTO> dtos) {
        return ResponseEntity.ok(service.replaceAllForAirline(airlineId, dtos));
    }
}
