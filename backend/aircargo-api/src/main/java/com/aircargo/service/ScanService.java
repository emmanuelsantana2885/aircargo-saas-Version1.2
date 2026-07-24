package com.aircargo.service;

import com.aircargo.dto.ScanLookupDTO;
import com.aircargo.dto.ScanPieceRequest;
import com.aircargo.dto.ScanPieceResult;
import com.aircargo.entity.*;
import com.aircargo.common.entity.CommodityType;
import com.aircargo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ScanService {

    private final MawbRepository mawbRepository;
    private final HawbRepository hawbRepository;
    private final UldRepository uldRepository;
    private final UldAwbRepository uldAwbRepository;
    private final UldPieceRepository uldPieceRepository;
    private final WarehouseReceiptRepository receiptRepository;
    private final BookingRepository bookingRepository;

    public ScanService(MawbRepository mawbRepository,
                       HawbRepository hawbRepository,
                       UldRepository uldRepository,
                       UldAwbRepository uldAwbRepository,
                       UldPieceRepository uldPieceRepository,
                       WarehouseReceiptRepository receiptRepository,
                       BookingRepository bookingRepository) {
        this.mawbRepository = mawbRepository;
        this.hawbRepository = hawbRepository;
        this.uldRepository = uldRepository;
        this.uldAwbRepository = uldAwbRepository;
        this.uldPieceRepository = uldPieceRepository;
        this.receiptRepository = receiptRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Lookup: resuelve un código escaneado (MAWB, HAWB, o ULD number).
     */
    @Transactional(readOnly = true)
    public ScanLookupDTO lookup(String code, UUID uldId) {
        String normalized = normalizeCode(code);

        // 1. Intentar como MAWB
        Optional<Mawb> mawbOpt = mawbRepository.findByAwbNumber(normalized);
        if (mawbOpt.isPresent()) {
            return buildMawbLookup(mawbOpt.get(), uldId);
        }

        // 2. Intentar como HAWB
        Optional<Hawb> hawbOpt = hawbRepository.findByHawbNumber(normalized);
        if (hawbOpt.isPresent()) {
            Hawb hawb = hawbOpt.get();
            Mawb parentMawb = hawb.getMawb();
            ScanLookupDTO dto = buildMawbLookup(parentMawb, uldId);
            dto.setType("HAWB");
            dto.setHawbNumber(hawb.getHawbNumber());
            dto.setHawbPieces(hawb.getPieces() != null ? hawb.getPieces() : 0);
            return dto;
        }

        // 3. Intentar como ULD number
        Optional<Uld> uldOpt = uldRepository.findAll().stream()
                .filter(u -> u.getUldNumber() != null && normalizeCode(u.getUldNumber()).equals(normalized))
                .findFirst();
        if (uldOpt.isPresent()) {
            return buildUldLookup(uldOpt.get());
        }

        // 4. No encontrado
        return null;
    }

    /**
     * Registrar una pieza escaneada o manual en un ULD.
     */
    @Transactional
    public ScanPieceResult registerPiece(ScanPieceRequest request, UUID scannedBy) {
        Uld uld = uldRepository.findById(request.getUldId())
                .orElseThrow(() -> new IllegalArgumentException("ULD no encontrada: " + request.getUldId()));

        String normalized = normalizeCode(request.getAwbNumber());

        // Resolver el código a un MAWB
        Mawb mawb = null;
        String hawbNumber = request.getHawbNumber();

        Optional<Mawb> mawbOpt = mawbRepository.findByAwbNumber(normalized);
        if (mawbOpt.isPresent()) {
            mawb = mawbOpt.get();
        } else {
            // Intentar como HAWB
            Optional<Hawb> hawbOpt = hawbRepository.findByHawbNumber(normalized);
            if (hawbOpt.isPresent()) {
                Hawb hawb = hawbOpt.get();
                mawb = hawb.getMawb();
                hawbNumber = hawb.getHawbNumber();
            }
        }

        if (mawb == null) {
            return errorResult("Código no reconocido: " + request.getAwbNumber());
        }

        // ─── WARN: MAWB without linked Booking ──────────────
        String bookingWarning = null;
        java.util.List<Booking> linkedBookings = bookingRepository.findByMawbId(mawb.getId());
        if (linkedBookings.isEmpty()) {
            bookingWarning = "MAWB " + mawb.getAwbNumber() + " no tiene Booking vinculado. " +
                    "El piezas escaneadas no se descontaran de ningun Booking.";
        }

        // Verificar límite de piezas
        long existingCount = uldPieceRepository.countByUldIdAndMawbId(uld.getId(), mawb.getId());
        int maxAllowed = getMaxPieces(mawb);

        if (existingCount >= maxAllowed && maxAllowed > 0) {
            return errorResult("Límite alcanzado: " + existingCount + "/" + maxAllowed +
                    " piezas para " + mawb.getAwbNumber());
        }

        // Determinar siguiente piece_number
        int nextPieceNumber = (int) existingCount + 1;

        // Crear UldPiece
        UldPiece piece = new UldPiece();
        piece.setUld(uld);
        piece.setMawb(mawb);
        piece.setAwbNumber(mawb.getAwbNumber());
        piece.setHawbNumber(hawbNumber);
        piece.setPieceNumber(nextPieceNumber);
        piece.setSource("BARCODE".equalsIgnoreCase(request.getSource()) ? PieceSource.BARCODE : PieceSource.MANUAL);
        piece.setScannedBy(scannedBy);
        piece.setScannedAt(OffsetDateTime.now());
        uldPieceRepository.save(piece);

        // Upsert UldAwb: crear o actualizar el conteo
        upsertUldAwb(uld, mawb, nextPieceNumber);

        // Auto-advance MAWB status
        if (mawb.getStatus() == MawbStatus.BOOKED || mawb.getStatus() == MawbStatus.RECEIVED) {
            mawb.setStatus(MawbStatus.MANIFESTED);
            mawbRepository.save(mawb);
        }

        ScanPieceResult result = new ScanPieceResult();
        result.setSuccess(true);
        result.setMessage("Pieza #" + nextPieceNumber + " registrada");
        result.setPieceNumber(nextPieceNumber);
        result.setAwbNumber(mawb.getAwbNumber());
        result.setTotalOnUld((int) existingCount + 1);
        result.setAvailablePieces(Math.max(0, maxAllowed - (int) existingCount - 1));
        result.setWarning(bookingWarning);
        return result;
    }

    /**
     * Deshacer la última pieza escaneada para un ULD+MAWB.
     */
    @Transactional
    public boolean undoLastPiece(UUID uldId, UUID mawbId) {
        Optional<UldPiece> lastOpt = uldPieceRepository.findFirstByUldIdAndMawbIdOrderByPieceNumberDesc(uldId, mawbId);
        if (lastOpt.isEmpty()) return false;

        UldPiece last = lastOpt.get();
        uldPieceRepository.delete(last);

        // Recalcular UldAwb.pieces
        long remaining = uldPieceRepository.countByUldIdAndMawbId(uldId, mawbId);
        uldAwbRepository.findByUldIdAndMawbId(uldId, mawbId).ifPresent(uldAwb -> {
            uldAwb.setPieces((int) remaining);
            uldAwbRepository.save(uldAwb);
        });

        return true;
    }

    // ── Helpers ──────────────────────────────────────────────

    private String normalizeCode(String code) {
        if (code == null) return "";
        // Quitar espacios, guiones, y pasar a mayúsculas
        return code.replaceAll("[\\s\\-]", "").toUpperCase();
    }

    private int getMaxPieces(Mawb mawb) {
        int reserved = mawb.getPieces() != null ? mawb.getPieces() : 0;

        int received = receiptRepository.findByMawbId(mawb.getId()).stream()
                .mapToInt(r -> r.getPieceCount() != null ? r.getPieceCount() : 0)
                .sum();

        int booked = bookingRepository.findByMawbId(mawb.getId()).stream()
                .mapToInt(b -> b.getSkids() != null ? b.getSkids() : 0)
                .max()
                .orElse(0);

        if (mawb.getStatus() == MawbStatus.RECEIVED ||
            mawb.getStatus() == MawbStatus.MANIFESTED ||
            mawb.getStatus() == MawbStatus.DEPARTED) {
            return Math.max(reserved, Math.max(booked, received));
        }
        return Math.max(reserved, booked);
    }

    private ScanLookupDTO buildMawbLookup(Mawb mawb, UUID uldId) {
        ScanLookupDTO dto = new ScanLookupDTO();
        dto.setType("MAWB");
        dto.setAwbNumber(mawb.getAwbNumber());
        dto.setMawbId(mawb.getId().toString());
        dto.setShipperName(mawb.getShipperName());
        dto.setConsigneeName(mawb.getConsigneeName());
        dto.setCommodityType(mawb.getCommodityType() != null ? mawb.getCommodityType().name() : "DRY_CARGO");
        dto.setDestination(mawb.getDestination());
        dto.setReservedPieces(mawb.getPieces() != null ? mawb.getPieces() : 0);

        int received = receiptRepository.findByMawbId(mawb.getId()).stream()
                .mapToInt(r -> r.getPieceCount() != null ? r.getPieceCount() : 0)
                .sum();
        dto.setReceivedPieces(received);

        int assigned = uldAwbRepository.findByMawbId(mawb.getId()).stream()
                .mapToInt(link -> link.getPieces() != null ? link.getPieces() : 0)
                .sum();
        dto.setAssignedTotal(assigned);

        int maxAllowed = getMaxPieces(mawb);
        dto.setAvailablePieces(Math.max(0, maxAllowed - assigned));

        if (uldId != null) {
            long existingOnUld = uldPieceRepository.countByUldIdAndMawbId(uldId, mawb.getId());
            dto.setExistingOnUld((int) existingOnUld);
        }

        return dto;
    }

    private ScanLookupDTO buildUldLookup(Uld uld) {
        ScanLookupDTO dto = new ScanLookupDTO();
        dto.setType("ULD");
        dto.setUldId(uld.getId().toString());
        dto.setUldNumber(uld.getUldNumber());
        dto.setUldType(uld.getUldType() != null ? uld.getUldType().name() : "UNK");
        dto.setFlightId(uld.getFlight() != null ? uld.getFlight().getId().toString() : null);
        dto.setStatus(uld.getStatus() != null ? uld.getStatus().name() : "OPEN");

        int totalPieces = uldPieceRepository.findByUldId(uld.getId()).size();
        dto.setCurrentPieces(totalPieces);
        return dto;
    }

    private void upsertUldAwb(Uld uld, Mawb mawb, int pieceCount) {
        Optional<UldAwb> existing = uldAwbRepository.findByUldIdAndMawbId(uld.getId(), mawb.getId());
        if (existing.isPresent()) {
            UldAwb awb = existing.get();
            awb.setPieces(pieceCount);
            uldAwbRepository.save(awb);
        } else {
            UldAwb awb = new UldAwb();
            awb.setUld(uld);
            awb.setMawb(mawb);
            awb.setMawbLabel(mawb.getAwbNumber());
            awb.setDescription(mawb.getCommodityType() != null ? mawb.getCommodityType() : CommodityType.DRY_CARGO);
            awb.setDestination(mawb.getDestination());
            awb.setPieces(pieceCount);
            awb.setPiecesPct(100);
            uldAwbRepository.save(awb);
        }
    }

    private ScanPieceResult errorResult(String message) {
        ScanPieceResult result = new ScanPieceResult();
        result.setSuccess(false);
        result.setError(message);
        return result;
    }
}
