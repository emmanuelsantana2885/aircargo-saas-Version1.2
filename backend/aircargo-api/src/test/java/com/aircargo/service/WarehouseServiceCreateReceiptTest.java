package com.aircargo.service;

import com.aircargo.entity.Mawb;
import com.aircargo.entity.ReceiptPiece;
import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.repository.BookingRepository;
import com.aircargo.repository.MawbRepository;
import com.aircargo.repository.ReceiptPieceRepository;
import com.aircargo.repository.WarehouseReceiptRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceCreateReceiptTest {

    @Mock private WarehouseReceiptRepository receiptRepository;
    @Mock private ReceiptPieceRepository pieceRepository;
    @Mock private MawbRepository mawbRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    private PdfGenerationService pdfService = new PdfGenerationService();
    @Mock private ReceiptExportService receiptExportService;

    private WarehouseService service;

    @BeforeEach
    void setUp() {
        service = new WarehouseService(
                receiptRepository, pieceRepository, mawbRepository,
                bookingRepository, new ObjectMapper(), eventPublisher,
                pdfService, receiptExportService);
    }

    private WarehouseReceipt makeBase(UUID mawbId) {
        WarehouseReceipt base = new WarehouseReceipt();
        Mawb mawb = new Mawb();
        mawb.setId(mawbId);
        mawb.setAwbNumber("123-45678901");
        base.setMawb(mawb);
        base.setGatewayCfs("SDQ");
        base.setShipperName("Test Shipper");
        base.setConsigneeName("Test Consignee");
        base.setOrigin("SDQ");
        base.setDestination("MIA");
        return base;
    }

    private ReceiptPiece makePiece(int qty) {
        ReceiptPiece p = new ReceiptPiece();
        p.setPieces(qty);
        p.setLengthIn(new BigDecimal("20"));
        p.setWidthIn(new BigDecimal("15"));
        p.setHeightIn(new BigDecimal("10"));
        p.setScaleWeightLbs(new BigDecimal("25.0"));
        p.setScaleWeightKg(new BigDecimal("11.340"));
        return p;
    }

    private void stubSaveAll() {
        when(receiptRepository.save(any(WarehouseReceipt.class)))
                .thenAnswer(inv -> {
                    WarehouseReceipt r = inv.getArgument(0);
                    if (r.getId() == null) r.setId(UUID.randomUUID());
                    return r;
                });
        when(pieceRepository.save(any(ReceiptPiece.class))).thenAnswer(inv -> inv.getArgument(0));
        doNothing().when(pieceRepository).flush();
    }

    @Test
    void createReceipt_supersedesExistingReceipts() {
        UUID mawbId = UUID.randomUUID();
        WarehouseReceipt base = makeBase(mawbId);

        WarehouseReceipt old1 = new WarehouseReceipt();
        old1.setId(UUID.randomUUID());
        old1.setSuperseded(false);
        old1.setPieceCount(3);

        WarehouseReceipt old2 = new WarehouseReceipt();
        old2.setId(UUID.randomUUID());
        old2.setSuperseded(false);
        old2.setPieceCount(5);

        when(receiptRepository.findByMawbIdAndSupersededFalse(mawbId))
                .thenReturn(List.of(old1, old2));
        when(mawbRepository.findById(mawbId)).thenReturn(Optional.empty());
        stubSaveAll();

        List<WarehouseReceipt> result = service.createReceipt(base, List.of(makePiece(2)), null);

        assertTrue(old1.getSuperseded(), "old1 should be superseded");
        assertTrue(old2.getSuperseded(), "old2 should be superseded");

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(receiptRepository).saveAll(captor.capture());
        List<WarehouseReceipt> saved = captor.getValue();
        assertEquals(2, saved.size());
        assertTrue(saved.get(0).getSuperseded());
        assertTrue(saved.get(1).getSuperseded());

        assertFalse(result.isEmpty(), "Should create new receipt");
    }

    @Test
    void createReceipt_noExistingReceipts_skipsSupersede() {
        UUID mawbId = UUID.randomUUID();
        WarehouseReceipt base = makeBase(mawbId);

        when(receiptRepository.findByMawbIdAndSupersededFalse(mawbId))
                .thenReturn(List.of());
        when(mawbRepository.findById(mawbId)).thenReturn(Optional.empty());
        stubSaveAll();

        List<WarehouseReceipt> result = service.createReceipt(base, List.of(makePiece(1)), null);

        assertFalse(result.isEmpty());
        verify(receiptRepository, never()).saveAll(anyList());
    }

    @Test
    void createReceipt_afterSupersede_findBySupersededFalseReturnsOnlyNew() {
        UUID mawbId = UUID.randomUUID();
        WarehouseReceipt base = makeBase(mawbId);

        WarehouseReceipt old = new WarehouseReceipt();
        old.setId(UUID.randomUUID());
        old.setSuperseded(false);
        old.setPieceCount(4);

        when(receiptRepository.findByMawbIdAndSupersededFalse(mawbId))
                .thenReturn(List.of(old));
        when(mawbRepository.findById(mawbId)).thenReturn(Optional.empty());
        stubSaveAll();

        service.createReceipt(base, List.of(makePiece(3)), null);
        assertTrue(old.getSuperseded(), "old receipt must be superseded");

        WarehouseReceipt newReceipt = new WarehouseReceipt();
        newReceipt.setId(UUID.randomUUID());
        newReceipt.setSuperseded(false);
        newReceipt.setPieceCount(3);

        when(receiptRepository.findByMawbIdAndSupersededFalse(mawbId))
                .thenReturn(List.of(newReceipt));

        service.createReceipt(makeBase(mawbId), List.of(makePiece(5)), null);
        assertTrue(newReceipt.getSuperseded(), "new receipt should now be superseded by second call");

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(receiptRepository, times(2)).saveAll(captor.capture());
        assertEquals(1, captor.getAllValues().get(0).size(), "first call should supersede 1 receipt");
        assertEquals(1, captor.getAllValues().get(1).size(), "second call should supersede 1 receipt");
    }

    @Test
    void createReceipt_groupsPiecesByHawbAndSupersedes() {
        UUID mawbId = UUID.randomUUID();
        WarehouseReceipt base = makeBase(mawbId);

        WarehouseReceipt existingGeneral = new WarehouseReceipt();
        existingGeneral.setId(UUID.randomUUID());
        existingGeneral.setSuperseded(false);

        when(receiptRepository.findByMawbIdAndSupersededFalse(mawbId))
                .thenReturn(List.of(existingGeneral));
        when(mawbRepository.findById(mawbId)).thenReturn(Optional.empty());
        stubSaveAll();

        UUID hawbId = UUID.randomUUID();
        ReceiptPiece generalPiece = makePiece(2);
        ReceiptPiece hawbPiece = makePiece(3);
        hawbPiece.setHawbId(hawbId);

        List<WarehouseReceipt> result = service.createReceipt(base, List.of(generalPiece, hawbPiece), null);

        assertTrue(existingGeneral.getSuperseded());
        assertEquals(2, result.size(), "Should create 2 receipt groups (general + HAWB)");
    }
}
