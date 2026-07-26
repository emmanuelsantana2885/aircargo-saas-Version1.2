package com.aircargo.service;

import com.aircargo.dto.WarehouseReceiptDTO;
import com.aircargo.common.entity.Airline;
import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.repository.WarehouseReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WarehouseReceiptServiceImplTest {

    @Mock
    private WarehouseReceiptRepository repository;

    private WarehouseReceiptServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new WarehouseReceiptServiceImpl(repository);
    }

    private WarehouseReceipt makeReceipt(UUID id, UUID airlineId) {
        WarehouseReceipt receipt = new WarehouseReceipt();
        receipt.setId(id);
        if (airlineId != null) {
            Airline airline = new Airline();
            airline.setId(airlineId);
            receipt.setAirline(airline);
        }
        receipt.setGatewayCfs("Gateway");
        receipt.setShipperName("Shipper");
        receipt.setConsigneeName("Consignee");
        receipt.setMawbWeightGreatest(new BigDecimal("50.0"));
        receipt.setReceiptDate(OffsetDateTime.now());
        return receipt;
    }

    @Test
    void getAll_filtersByAirlineId() {
        UUID airlineId = UUID.randomUUID();
        WarehouseReceipt receipt = makeReceipt(UUID.randomUUID(), airlineId);
        when(repository.findByAirlineIdAndSupersededFalse(airlineId)).thenReturn(List.of(receipt));

        List<WarehouseReceiptDTO> result = service.getAll(airlineId);

        assertEquals(1, result.size());
        assertEquals(airlineId, result.get(0).getAirlineId());
        verify(repository).findByAirlineIdAndSupersededFalse(airlineId);
    }

    @Test
    void getById_returnsReceipt() {
        UUID id = UUID.randomUUID();
        WarehouseReceipt receipt = makeReceipt(id, UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Optional.of(receipt));

        Optional<WarehouseReceiptDTO> result = service.getById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Gateway", result.get().getGatewayCfs());
    }

    @Test
    void create_savesReceipt() {
        WarehouseReceiptDTO dto = new WarehouseReceiptDTO();
        dto.setGatewayCfs("New Gateway");
        dto.setShipperName("New Shipper");
        dto.setConsigneeName("New Consignee");
        dto.setMawbWeightGreatest(new BigDecimal("25.0"));

        WarehouseReceipt saved = makeReceipt(UUID.randomUUID(), UUID.randomUUID());
        saved.setGatewayCfs("New Gateway");
        when(repository.save(any())).thenReturn(saved);

        WarehouseReceiptDTO result = service.create(dto);

        assertNotNull(result);
        assertEquals("New Gateway", result.getGatewayCfs());
        verify(repository).save(any(WarehouseReceipt.class));
    }

    @Test
    void update_existingReceipt() {
        UUID id = UUID.randomUUID();
        WarehouseReceipt existing = makeReceipt(id, UUID.randomUUID());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WarehouseReceiptDTO dto = new WarehouseReceiptDTO();
        dto.setGatewayCfs("Updated Gateway");

        Optional<WarehouseReceiptDTO> result = service.update(id, dto);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Updated Gateway", result.get().getGatewayCfs());
        verify(repository).save(any(WarehouseReceipt.class));
    }

    @Test
    void delete_existingReceipt() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);

        boolean deleted = service.delete(id);

        assertTrue(deleted);
        verify(repository).deleteById(id);
    }
}
