package com.aircargo.warehouseservice.service;

import com.aircargo.warehouseservice.dto.WarehouseReceiptDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseReceiptService {
    List<WarehouseReceiptDTO> getAll();
    Optional<WarehouseReceiptDTO> getById(UUID id);
    WarehouseReceiptDTO save(WarehouseReceiptDTO dto);
    void delete(UUID id);
}