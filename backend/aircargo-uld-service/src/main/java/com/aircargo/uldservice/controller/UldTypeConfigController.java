package com.aircargo.uldservice.controller;

import com.aircargo.uldservice.entity.UldTypeConfig;
import com.aircargo.uldservice.repository.UldTypeConfigRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/uld-type-config")
public class UldTypeConfigController {

    private final UldTypeConfigRepository repository;

    public UldTypeConfigController(UldTypeConfigRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{airlineId}")
    public ResponseEntity<List<UldTypeConfig>> getByAirline(@PathVariable UUID airlineId) {
        return ResponseEntity.ok(repository.findByAirlineId(airlineId));
    }
}
