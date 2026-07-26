package com.aircargo.service;

import com.aircargo.dto.WarehouseReceiptDTO;
import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.repository.WarehouseReceiptRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WarehouseReceiptServiceImpl implements WarehouseReceiptService{

    private final WarehouseReceiptRepository repository;

    public WarehouseReceiptServiceImpl(WarehouseReceiptRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<WarehouseReceiptDTO> getAll(UUID airlineId) {
        List<WarehouseReceipt> results = airlineId != null
                ? repository.findByAirlineIdAndSupersededFalse(airlineId)
                : repository.findBySupersededFalse();
        return results.stream().map(WarehouseReceiptDTO::fromEntity).collect(Collectors.toList());
    }

    @Override
    public Optional<WarehouseReceiptDTO> getById(UUID id) {
        return repository.findById(id).map(WarehouseReceiptDTO::fromEntity);
    }

    @Override
    public WarehouseReceiptDTO create(WarehouseReceiptDTO dto) {
        WarehouseReceipt e = WarehouseReceiptDTO.toEntity(dto);
        WarehouseReceipt saved = repository.save(e);
        return WarehouseReceiptDTO.fromEntity(saved);
    }

    @Override
    public Optional<WarehouseReceiptDTO> update(UUID id, WarehouseReceiptDTO dto) {
        return repository.findById(id)
                .map(existing -> {
                    WarehouseReceipt updated = WarehouseReceiptDTO.toEntity(dto);
                    updated.setId(existing.getId());
                    return repository.save(updated);
                })
                .map(WarehouseReceiptDTO::fromEntity);
    }

    @Override
    public boolean delete(UUID id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
