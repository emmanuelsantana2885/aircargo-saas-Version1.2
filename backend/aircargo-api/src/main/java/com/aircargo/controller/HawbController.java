package com.aircargo.controller;

import com.aircargo.dto.HawbDTO;
import com.aircargo.dto.PageResponse;
import com.aircargo.entity.Hawb;
import com.aircargo.entity.Mawb;
import com.aircargo.repository.HawbRepository;
import com.aircargo.repository.MawbRepository;
import com.aircargo.service.HawbValidationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cargo/hawbs")
public class HawbController {

    private final HawbRepository hawbRepository;
    private final MawbRepository mawbRepository;
    private final HawbValidationService validationService;

    public HawbController(HawbRepository hawbRepository, MawbRepository mawbRepository, HawbValidationService validationService) {
        this.hawbRepository = hawbRepository;
        this.mawbRepository = mawbRepository;
        this.validationService = validationService;
    }

    /**
     * Endpoint para listar el desglose completo de House AWBs de una guía maestra.
     */
    @GetMapping("/mawb/{mawbId}")
    public ResponseEntity<List<HawbDTO>> getHawbsByMawb(@PathVariable UUID mawbId) {
        return ResponseEntity.ok(hawbRepository.findByMawbId(mawbId).stream()
                .map(HawbDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList()));
    }

    /**
     * Endpoint para listar HAWBs de una MAWB con paginación.
     */
    @GetMapping(value = "/mawb/{mawbId}", params = {"page", "size"})
    public PageResponse<HawbDTO> getHawbsByMawbPaginated(
            @PathVariable UUID mawbId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageReq = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Hawb> result = hawbRepository.findByMawbId(mawbId, pageReq);
        List<HawbDTO> dtos = result.getContent().stream()
                .map(HawbDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
        return PageResponse.of(dtos, page, size, result.getTotalElements());
    }

    /**
     * Endpoint para actualizar campos de una HAWB existente (consignee, destination, pieces, weight).
     */
    @PutMapping("/{hawbId}")
    public ResponseEntity<?> updateHawb(@PathVariable UUID hawbId, @Valid @RequestBody Hawb updates) {
        return hawbRepository.findById(hawbId).map(hawb -> {
            if (updates.getConsigneeName() != null) hawb.setConsigneeName(updates.getConsigneeName());
            if (updates.getDestination() != null) hawb.setDestination(updates.getDestination());
            if (updates.getPieces() != null) hawb.setPieces(updates.getPieces());
            if (updates.getWeightKg() != null) hawb.setWeightKg(updates.getWeightKg());
            if (updates.getCommodityType() != null) hawb.setCommodityType(updates.getCommodityType());
            if (updates.getNotes() != null) hawb.setNotes(updates.getNotes());
            return ResponseEntity.ok(HawbDTO.fromEntity(hawbRepository.save(hawb)));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lightweight request object for creating a HAWB — prevents mass assignment.
     */
    public static class HawbCreateRequest {
        @NotNull(message = "mawbId es requerido")
        public UUID mawbId;
        public String hawbNumber;
        public String consigneeName;
        public String destination;
        public Integer pieces;
        public java.math.BigDecimal weightKg;
        public com.aircargo.common.entity.CommodityType commodityType;
        public String notes;
    }

    /**
     * Endpoint para inyectar una House AWB auditando que encaje perfectamente en la MAWB madre.
     */
    @PostMapping
    public ResponseEntity<?> createHawb(@Valid @RequestBody HawbCreateRequest req) {
        try {
            Mawb mawb = mawbRepository.findById(req.mawbId)
                    .orElse(null);
            if (mawb == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "MAWB no encontrada: " + req.mawbId));
            }

            double weight = req.weightKg != null ? req.weightKg.doubleValue() : 0.0;
            int pieces = req.pieces != null ? req.pieces : 0;

            validationService.validateHawbConsolidation(req.mawbId, pieces, weight);

            Hawb hawb = new Hawb();
            hawb.setMawb(mawb);
            hawb.setHawbNumber(req.hawbNumber);
            hawb.setConsigneeName(req.consigneeName);
            hawb.setDestination(req.destination);
            hawb.setPieces(req.pieces);
            hawb.setWeightKg(req.weightKg);
            if (req.commodityType != null) hawb.setCommodityType(req.commodityType);
            hawb.setNotes(req.notes);

            Hawb savedHawb = hawbRepository.save(hawb);
            return ResponseEntity.ok(HawbDTO.fromEntity(savedHawb));

        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.status(422).body(Map.of("success", false, "error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Error en persistencia de HAWB: " + ex.getMessage()));
        }
    }
}
