package com.aircargo.service;

import com.aircargo.dto.ScanLookupDTO;
import com.aircargo.dto.ScanPieceRequest;
import com.aircargo.dto.ScanPieceResult;
import com.aircargo.common.entity.CommodityType;
import com.aircargo.entity.*;
import com.aircargo.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScanServiceTest {

    @Mock private MawbRepository mawbRepository;
    @Mock private HawbRepository hawbRepository;
    @Mock private UldRepository uldRepository;
    @Mock private UldAwbRepository uldAwbRepository;
    @Mock private UldPieceRepository uldPieceRepository;
    @Mock private WarehouseReceiptRepository receiptRepository;

    private ScanService service;

    private UUID uldId = UUID.randomUUID();
    private UUID mawbId = UUID.randomUUID();
    private UUID hawbId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ScanService(mawbRepository, hawbRepository, uldRepository,
                uldAwbRepository, uldPieceRepository, receiptRepository);
    }

    private Mawb makeMawb(String awbNumber, int pieces, MawbStatus status) {
        Mawb m = new Mawb();
        m.setId(mawbId);
        m.setAwbNumber(awbNumber);
        m.setPieces(pieces);
        m.setStatus(status);
        m.setCommodityType(CommodityType.DRY_CARGO);
        m.setDestination("MIA");
        m.setShipperName("Test Shipper");
        m.setConsigneeName("Test Consignee");
        return m;
    }

    private Uld makeUld(String uldNumber) {
        Uld u = new Uld();
        u.setId(uldId);
        u.setUldNumber(uldNumber);
        u.setUldType(UldType.PMC);
        u.setStatus(UldStatus.OPEN);
        return u;
    }

    // ── LOOKUP TESTS ────────────────────────────────────────

    @Test
    void lookup_mawb_found() {
        Mawb mawb = makeMawb("00012345678", 12, MawbStatus.BOOKED);
        when(mawbRepository.findByAwbNumber("00012345678")).thenReturn(Optional.of(mawb));
        when(receiptRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());
        when(uldAwbRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());

        ScanLookupDTO result = service.lookup("000-12345678", null);

        assertNotNull(result);
        assertEquals("MAWB", result.getType());
        assertEquals("00012345678", result.getAwbNumber());
        assertEquals(12, result.getReservedPieces());
        assertEquals(0, result.getReceivedPieces());
        assertEquals(0, result.getAssignedTotal());
        assertEquals(12, result.getAvailablePieces());
    }

    @Test
    void lookup_mawb_with_dashes_normalized() {
        Mawb mawb = makeMawb("01612345678", 5, MawbStatus.RECEIVED);
        when(mawbRepository.findByAwbNumber("01612345678")).thenReturn(Optional.of(mawb));
        when(receiptRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());
        when(uldAwbRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());

        ScanLookupDTO result = service.lookup("016-12345678", null);

        assertNotNull(result);
        assertEquals("MAWB", result.getType());
        assertEquals(5, result.getReservedPieces());
    }

    @Test
    void lookup_hawb_resolves_to_parent_mawb() {
        Mawb parentMawb = makeMawb("00012345678", 10, MawbStatus.BOOKED);
        Hawb hawb = new Hawb();
        hawb.setId(hawbId);
        hawb.setHawbNumber("HAWB999");
        hawb.setMawb(parentMawb);
        hawb.setPieces(3);

        when(mawbRepository.findByAwbNumber("HAWB999")).thenReturn(Optional.empty());
        when(hawbRepository.findByHawbNumber("HAWB999")).thenReturn(Optional.of(hawb));
        when(receiptRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());
        when(uldAwbRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());

        ScanLookupDTO result = service.lookup("HAWB999", null);

        assertNotNull(result);
        assertEquals("HAWB", result.getType());
        assertEquals("00012345678", result.getAwbNumber());
        assertEquals("HAWB999", result.getHawbNumber());
        assertEquals(3, result.getHawbPieces());
    }

    @Test
    void lookup_uld_found() {
        Uld uld = makeUld("PMC-12345");
        when(mawbRepository.findByAwbNumber(any())).thenReturn(Optional.empty());
        when(hawbRepository.findByHawbNumber(any())).thenReturn(Optional.empty());
        when(uldRepository.findAll()).thenReturn(List.of(uld));
        when(uldPieceRepository.findByUldId(uldId)).thenReturn(Collections.emptyList());

        ScanLookupDTO result = service.lookup("PMC-12345", null);

        assertNotNull(result);
        assertEquals("ULD", result.getType());
        assertEquals("PMC-12345", result.getUldNumber());
        assertEquals("PMC", result.getUldType());
        assertEquals(0, result.getCurrentPieces());
    }

    @Test
    void lookup_not_found() {
        when(mawbRepository.findByAwbNumber(any())).thenReturn(Optional.empty());
        when(hawbRepository.findByHawbNumber(any())).thenReturn(Optional.empty());
        when(uldRepository.findAll()).thenReturn(Collections.emptyList());

        ScanLookupDTO result = service.lookup("UNKNOWN123", null);

        assertNull(result);
    }

    @Test
    void lookup_existing_on_uld() {
        Mawb mawb = makeMawb("00012345678", 12, MawbStatus.BOOKED);
        when(mawbRepository.findByAwbNumber("00012345678")).thenReturn(Optional.of(mawb));
        when(receiptRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());
        when(uldAwbRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());
        when(uldPieceRepository.countByUldIdAndMawbId(uldId, mawbId)).thenReturn(3L);

        ScanLookupDTO result = service.lookup("00012345678", uldId);

        assertNotNull(result);
        assertEquals(3, result.getExistingOnUld());
    }

    // ── REGISTER PIECE TESTS ───────────────────────────────

    @Test
    void registerPiece_valid_barcode() {
        Mawb mawb = makeMawb("00012345678", 12, MawbStatus.BOOKED);
        Uld uld = makeUld("PMC-12345");

        when(uldRepository.findById(uldId)).thenReturn(Optional.of(uld));
        when(mawbRepository.findByAwbNumber("00012345678")).thenReturn(Optional.of(mawb));
        when(uldPieceRepository.countByUldIdAndMawbId(uldId, mawbId)).thenReturn(0L);
        when(receiptRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());
        when(uldAwbRepository.findByUldIdAndMawbId(uldId, mawbId)).thenReturn(Optional.empty());
        when(uldPieceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(uldAwbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScanPieceRequest req = new ScanPieceRequest();
        req.setUldId(uldId);
        req.setAwbNumber("000-12345678");
        req.setSource("BARCODE");

        ScanPieceResult result = service.registerPiece(req, UUID.randomUUID());

        assertTrue(result.isSuccess());
        assertEquals(1, result.getPieceNumber());
        assertEquals("00012345678", result.getAwbNumber());
        assertEquals(1, result.getTotalOnUld());
        assertEquals(11, result.getAvailablePieces());

        // Verify UldPiece was saved
        ArgumentCaptor<UldPiece> captor = ArgumentCaptor.forClass(UldPiece.class);
        verify(uldPieceRepository).save(captor.capture());
        UldPiece saved = captor.getValue();
        assertEquals(PieceSource.BARCODE, saved.getSource());
        assertEquals(1, saved.getPieceNumber());

        // Verify UldAwb was created
        verify(uldAwbRepository).save(any(UldAwb.class));
    }

    @Test
    void registerPiece_second_piece_increments() {
        Mawb mawb = makeMawb("00012345678", 12, MawbStatus.BOOKED);
        Uld uld = makeUld("PMC-12345");

        when(uldRepository.findById(uldId)).thenReturn(Optional.of(uld));
        when(mawbRepository.findByAwbNumber("00012345678")).thenReturn(Optional.of(mawb));
        when(uldPieceRepository.countByUldIdAndMawbId(uldId, mawbId)).thenReturn(1L);
        when(receiptRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());

        UldAwb existingAwb = new UldAwb();
        existingAwb.setPieces(1);
        when(uldAwbRepository.findByUldIdAndMawbId(uldId, mawbId)).thenReturn(Optional.of(existingAwb));
        when(uldPieceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(uldAwbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScanPieceRequest req = new ScanPieceRequest();
        req.setUldId(uldId);
        req.setAwbNumber("000-12345678");
        req.setSource("BARCODE");

        ScanPieceResult result = service.registerPiece(req, UUID.randomUUID());

        assertTrue(result.isSuccess());
        assertEquals(2, result.getPieceNumber());
        assertEquals(2, result.getTotalOnUld());
        assertEquals(10, result.getAvailablePieces());
    }

    @Test
    void registerPiece_limit_exceeded() {
        Mawb mawb = makeMawb("00012345678", 3, MawbStatus.BOOKED);
        Uld uld = makeUld("PMC-12345");

        when(uldRepository.findById(uldId)).thenReturn(Optional.of(uld));
        when(mawbRepository.findByAwbNumber("00012345678")).thenReturn(Optional.of(mawb));
        when(uldPieceRepository.countByUldIdAndMawbId(uldId, mawbId)).thenReturn(3L);
        when(receiptRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());

        ScanPieceRequest req = new ScanPieceRequest();
        req.setUldId(uldId);
        req.setAwbNumber("000-12345678");
        req.setSource("BARCODE");

        ScanPieceResult result = service.registerPiece(req, UUID.randomUUID());

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("Límite alcanzado"));
    }

    @Test
    void registerPiece_unknown_code() {
        when(uldRepository.findById(uldId)).thenReturn(Optional.of(makeUld("PMC-12345")));
        when(mawbRepository.findByAwbNumber("UNKNOWN")).thenReturn(Optional.empty());
        when(hawbRepository.findByHawbNumber("UNKNOWN")).thenReturn(Optional.empty());

        ScanPieceRequest req = new ScanPieceRequest();
        req.setUldId(uldId);
        req.setAwbNumber("UNKNOWN");
        req.setSource("BARCODE");

        ScanPieceResult result = service.registerPiece(req, UUID.randomUUID());

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("no reconocido"));
    }

    @Test
    void registerPiece_auto_advances_mawb_status() {
        Mawb mawb = makeMawb("00012345678", 12, MawbStatus.BOOKED);
        Uld uld = makeUld("PMC-12345");

        when(uldRepository.findById(uldId)).thenReturn(Optional.of(uld));
        when(mawbRepository.findByAwbNumber("00012345678")).thenReturn(Optional.of(mawb));
        when(uldPieceRepository.countByUldIdAndMawbId(uldId, mawbId)).thenReturn(0L);
        when(receiptRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());
        when(uldAwbRepository.findByUldIdAndMawbId(uldId, mawbId)).thenReturn(Optional.empty());
        when(uldPieceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(uldAwbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScanPieceRequest req = new ScanPieceRequest();
        req.setUldId(uldId);
        req.setAwbNumber("000-12345678");
        req.setSource("BARCODE");

        service.registerPiece(req, UUID.randomUUID());

        // Verify MAWB status was advanced to MANIFESTED
        verify(mawbRepository).save(argThat(m -> m.getStatus() == MawbStatus.MANIFESTED));
    }

    @Test
    void registerPiece_via_hawb() {
        Mawb parentMawb = makeMawb("00012345678", 10, MawbStatus.BOOKED);
        Hawb hawb = new Hawb();
        hawb.setId(hawbId);
        hawb.setHawbNumber("HAWB555");
        hawb.setMawb(parentMawb);
        hawb.setPieces(3);
        Uld uld = makeUld("PMC-12345");

        when(uldRepository.findById(uldId)).thenReturn(Optional.of(uld));
        when(mawbRepository.findByAwbNumber("HAWB555")).thenReturn(Optional.empty());
        when(hawbRepository.findByHawbNumber("HAWB555")).thenReturn(Optional.of(hawb));
        when(uldPieceRepository.countByUldIdAndMawbId(uldId, mawbId)).thenReturn(0L);
        when(receiptRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());
        when(uldAwbRepository.findByUldIdAndMawbId(uldId, mawbId)).thenReturn(Optional.empty());
        when(uldPieceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(uldAwbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScanPieceRequest req = new ScanPieceRequest();
        req.setUldId(uldId);
        req.setAwbNumber("HAWB555");
        req.setSource("BARCODE");

        ScanPieceResult result = service.registerPiece(req, UUID.randomUUID());

        assertTrue(result.isSuccess());
        assertEquals("00012345678", result.getAwbNumber());

        // Verify the UldPiece has the HAWB number
        ArgumentCaptor<UldPiece> captor = ArgumentCaptor.forClass(UldPiece.class);
        verify(uldPieceRepository).save(captor.capture());
        assertEquals("HAWB555", captor.getValue().getHawbNumber());
    }

    @Test
    void registerPiece_manual_source() {
        Mawb mawb = makeMawb("00012345678", 12, MawbStatus.BOOKED);
        Uld uld = makeUld("PMC-12345");

        when(uldRepository.findById(uldId)).thenReturn(Optional.of(uld));
        when(mawbRepository.findByAwbNumber("00012345678")).thenReturn(Optional.of(mawb));
        when(uldPieceRepository.countByUldIdAndMawbId(uldId, mawbId)).thenReturn(0L);
        when(receiptRepository.findByMawbId(mawbId)).thenReturn(Collections.emptyList());
        when(uldAwbRepository.findByUldIdAndMawbId(uldId, mawbId)).thenReturn(Optional.empty());
        when(uldPieceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(uldAwbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScanPieceRequest req = new ScanPieceRequest();
        req.setUldId(uldId);
        req.setAwbNumber("000-12345678");
        req.setSource("MANUAL");

        service.registerPiece(req, UUID.randomUUID());

        ArgumentCaptor<UldPiece> captor = ArgumentCaptor.forClass(UldPiece.class);
        verify(uldPieceRepository).save(captor.capture());
        assertEquals(PieceSource.MANUAL, captor.getValue().getSource());
    }

    // ── UNDO TESTS ─────────────────────────────────────────

    @Test
    void undoLastPiece_success() {
        UldPiece lastPiece = new UldPiece();
        lastPiece.setId(UUID.randomUUID());
        lastPiece.setPieceNumber(3);
        when(uldPieceRepository.findFirstByUldIdAndMawbIdOrderByPieceNumberDesc(uldId, mawbId))
                .thenReturn(Optional.of(lastPiece));

        UldAwb existingAwb = new UldAwb();
        existingAwb.setPieces(3);
        when(uldAwbRepository.findByUldIdAndMawbId(uldId, mawbId)).thenReturn(Optional.of(existingAwb));
        when(uldPieceRepository.countByUldIdAndMawbId(uldId, mawbId)).thenReturn(2L);

        boolean result = service.undoLastPiece(uldId, mawbId);

        assertTrue(result);
        verify(uldPieceRepository).delete(lastPiece);

        ArgumentCaptor<UldAwb> captor = ArgumentCaptor.forClass(UldAwb.class);
        verify(uldAwbRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getPieces());
    }

    @Test
    void undoLastPiece_no_pieces() {
        when(uldPieceRepository.findFirstByUldIdAndMawbIdOrderByPieceNumberDesc(uldId, mawbId))
                .thenReturn(Optional.empty());

        boolean result = service.undoLastPiece(uldId, mawbId);

        assertFalse(result);
        verify(uldPieceRepository, never()).delete(any());
    }

    // ── MAX PIECE LOGIC TESTS ──────────────────────────────

    @Test
    void registerPiece_received_status_uses_received_if_higher() {
        // MAWB reserved=5, received=8 → maxAllowed=8
        Mawb mawb = makeMawb("00012345678", 5, MawbStatus.RECEIVED);
        Uld uld = makeUld("PMC-12345");

        WarehouseReceipt receipt = new WarehouseReceipt();
        receipt.setPieceCount(8);
        when(uldRepository.findById(uldId)).thenReturn(Optional.of(uld));
        when(mawbRepository.findByAwbNumber("00012345678")).thenReturn(Optional.of(mawb));
        when(uldPieceRepository.countByUldIdAndMawbId(uldId, mawbId)).thenReturn(7L);
        when(receiptRepository.findByMawbId(mawbId)).thenReturn(List.of(receipt));

        ScanPieceRequest req = new ScanPieceRequest();
        req.setUldId(uldId);
        req.setAwbNumber("000-12345678");
        req.setSource("BARCODE");

        // 7 existing + 1 new = 8 ≤ 8 → should succeed
        when(uldAwbRepository.findByUldIdAndMawbId(uldId, mawbId)).thenReturn(Optional.empty());
        when(uldPieceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(uldAwbRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScanPieceResult result = service.registerPiece(req, UUID.randomUUID());

        assertTrue(result.isSuccess());
        assertEquals(8, result.getTotalOnUld());
        assertEquals(0, result.getAvailablePieces());
    }

    @Test
    void registerPiece_no_uld_throws() {
        when(uldRepository.findById(uldId)).thenReturn(Optional.empty());

        ScanPieceRequest req = new ScanPieceRequest();
        req.setUldId(uldId);
        req.setAwbNumber("000-12345678");

        assertThrows(IllegalArgumentException.class, () -> service.registerPiece(req, UUID.randomUUID()));
    }
}
