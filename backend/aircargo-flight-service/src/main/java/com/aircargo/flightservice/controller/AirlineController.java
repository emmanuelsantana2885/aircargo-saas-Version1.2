package com.aircargo.flightservice.controller;

import com.aircargo.flightservice.dto.AirlineDTO;
import com.aircargo.flightservice.service.AirlineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/airlines")
public class AirlineController {

    private final AirlineService airlineService;

    public AirlineController(AirlineService airlineService) {
        this.airlineService = airlineService;
    }

    @GetMapping
    public List<AirlineDTO> getAll() {
        return airlineService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AirlineDTO> getById(@PathVariable UUID id) {
        return airlineService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AirlineDTO> create(@Valid @RequestBody AirlineDTO dto) {
        AirlineDTO created = airlineService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AirlineDTO> update(@PathVariable UUID id, @Valid @RequestBody AirlineDTO dto) {
        return airlineService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean removed = airlineService.delete(id);
        if (!removed) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}