package com.aircargo.service;

import com.aircargo.entity.Booking;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.entity.ReceiptPiece;
import com.aircargo.common.event.ReceiptCreatedEvent;
import com.aircargo.repository.BookingRepository;
import com.aircargo.service.PdfGenerationService;
import com.aircargo.repository.MawbRepository;
import com.aircargo.repository.WarehouseReceiptRepository;
import com.aircargo.repository.ReceiptPieceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseService.class);

    private final WarehouseReceiptRepository receiptRepository;
    private final ReceiptPieceRepository pieceRepository;
    private final MawbRepository mawbRepository;
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final PdfGenerationService pdfService;
    private final ReceiptExportService receiptExportService;

    // Lock en memoria por MAWB para evitar condiciones de carrera cuando dos usuarios
    // emiten/actualizan el recibo del mismo MAWB casi al mismo tiempo (evita "lost update").
    // NOTA: esto serializa solo dentro de esta instancia de la app. Si en el futuro se
    // despliega en mas de una instancia (horizontal scaling), reemplazar por un lock
    // a nivel de base de datos (SELECT ... FOR UPDATE sobre la fila del MAWB).
    private final java.util.concurrent.ConcurrentHashMap<UUID, Object> mawbLocks = new java.util.concurrent.ConcurrentHashMap<>();

    private Object lockFor(UUID mawbId) {
        return mawbLocks.computeIfAbsent(mawbId, k -> new Object());
    }

    /**
     * Sincroniza el awbNumber del MAWB hacia todos los bookings vinculados.
     */
    private void syncBookingsAwbNumber(UUID mawbId) {
        if (mawbId == null) return;
        mawbRepository.findById(mawbId).ifPresent(mawb -> {
            List<Booking> bookings = bookingRepository.findByMawbId(mawbId);
            for (Booking bk : bookings) {
                bk.setAwbNumber(mawb.getAwbNumber());
                bookingRepository.save(bk);
            }
        });
    }

    /**
     * Recalcula fulfillment de bookings vinculados al MAWB sumando piezas y pesos de TODOS
     * los recibos de bodega del MAWB.
     * Si las piezas recibidas superan las reservadas (skids), actualiza el booking para
     * reflejar la cantidad fisica real.
     * @return lista de mensajes de correccion aplicadas (vacia si no hubo cambios)
     */
    private List<String> recalculateBookingFulfillment(UUID mawbId) {
        List<String> corrections = new java.util.ArrayList<>();
        if (mawbId == null) return corrections;
        List<WarehouseReceipt> allReceipts = receiptRepository.findByMawbId(mawbId);
        // Usar SUM de todos los recibos: cada recibo contiene un conjunto unico de piezas
        // (el recibo general tiene todas las piezas, no hay duplicacion).
        int totalReceivedPieces = allReceipts.stream()
                .mapToInt(r -> r.getPieceCount() != null ? r.getPieceCount() : 0)
                .sum();
        BigDecimal totalReceivedKg = allReceipts.stream()
                .map(r -> r.getChargeableWeightKg() != null ? r.getChargeableWeightKg() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Booking> bookings = bookingRepository.findByMawbId(mawbId);
        for (Booking bk : bookings) {
            bk.setReceivedKg(totalReceivedKg);
            int reservedSkids = bk.getSkids() != null ? bk.getSkids() : 0;

            // Si las piezas recibidas superan las reservadas, corregir el booking
            if (totalReceivedPieces > reservedSkids) {
                bk.setSkids(totalReceivedPieces);
                corrections.add("Booking corregido: " + reservedSkids + " → " + totalReceivedPieces + " skids (recibidas " + totalReceivedPieces + " piezas fisicas)");
                reservedSkids = totalReceivedPieces;
            }

            if (reservedSkids > 0) {
                BigDecimal pct = BigDecimal.valueOf(totalReceivedPieces)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(reservedSkids), 4, RoundingMode.HALF_UP);
                // Cap at 9999.9999 to avoid precision 8, scale 4 overflow
                if (pct.compareTo(new BigDecimal("9999.9999")) > 0) {
                    pct = new BigDecimal("9999.9999");
                }
                bk.setFulfillmentPct(pct);
            } else {
                bk.setFulfillmentPct(BigDecimal.ZERO);
            }
            bookingRepository.save(bk);
        }
        return corrections;
    }

    /**
     * Valida si emitir un recibo provocaria correcciones en el Booking.
     * NO guarda nada — solo calcula y retorna las correcciones potenciales.
     * Usado por el endpoint /validate para mostrar dialogo de confirmacion al usuario.
     */
    public List<String> validateBookingCorrections(UUID mawbId, int newPieceCount) {
        List<String> corrections = new java.util.ArrayList<>();
        if (mawbId == null) return corrections;

        // Sumar piezas existentes de recibos previos (no incluir el nuevo aun)
        List<WarehouseReceipt> existingReceipts = receiptRepository.findByMawbId(mawbId);
        int currentReceivedPieces = existingReceipts.stream()
                .mapToInt(r -> r.getPieceCount() != null ? r.getPieceCount() : 0)
                .sum();

        // Proyectar el total DESPUES de emitir el nuevo recibo
        int projectedTotal = currentReceivedPieces + newPieceCount;

        List<Booking> bookings = bookingRepository.findByMawbId(mawbId);
        for (Booking bk : bookings) {
            int reservedSkids = bk.getSkids() != null ? bk.getSkids() : 0;
            if (projectedTotal > reservedSkids) {
                corrections.add("Booking corregido: " + reservedSkids + " → " + projectedTotal + " skids (recibidas " + projectedTotal + " piezas fisicas)");
            }
        }
        return corrections;
    }

    /**
     * Recalcula piezas y peso facturable del MAWB sumando TODOS sus recibos de bodega.
     * El total de piezas = suma de pieceCount de todos los recibos.
     * El peso recibido = suma de mawbWeightGreatest (total scale lbs) de todos los recibos.
     * El recibo general contiene todas las piezas; al editar se reemplazan las piezas
     * (no se acumulan), por lo que SUM refleja el total real.
     */
    private void recalculateMawbTotals(UUID mawbId, WarehouseReceipt sourceReceipt) {
        if (mawbId == null) return;
        mawbRepository.findById(mawbId).ifPresent(mawb -> {
            List<WarehouseReceipt> allReceipts = receiptRepository.findByMawbId(mawbId);

            int totalPieces = allReceipts.stream()
                    .mapToInt(r -> r.getPieceCount() != null ? r.getPieceCount() : 0)
                    .sum();
            mawb.setPieces(totalPieces);

            if (totalPieces > 0) {
                mawb.setStatus(MawbStatus.RECEIVED);
            }

            BigDecimal totalChargeableKg = allReceipts.stream()
                    .map(r -> r.getChargeableWeightKg() != null ? r.getChargeableWeightKg() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            mawb.setChargeableWeightKg(totalChargeableKg);

            BigDecimal lbsToKg = BigDecimal.valueOf(0.45359237);
            BigDecimal greatestLbs = allReceipts.stream()
                    .map(r -> r.getMawbWeightGreatest() != null ? r.getMawbWeightGreatest() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (greatestLbs.compareTo(BigDecimal.ZERO) > 0) {
                mawb.setReportedWeightKg(greatestLbs.multiply(lbsToKg).setScale(3, RoundingMode.HALF_UP));
            } else {
                BigDecimal totalActualKg = allReceipts.stream()
                        .map(r -> r.getActualWeightKg() != null ? r.getActualWeightKg() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (totalActualKg.compareTo(BigDecimal.ZERO) > 0) {
                    mawb.setReportedWeightKg(totalActualKg);
                }
            }
            mawbRepository.save(mawb);
        });
    }

    public WarehouseService(WarehouseReceiptRepository receiptRepository,
                             ReceiptPieceRepository pieceRepository,
                             MawbRepository mawbRepository,
                             BookingRepository bookingRepository,
                             ObjectMapper objectMapper,
                             ApplicationEventPublisher eventPublisher,
                             PdfGenerationService pdfService,
                             ReceiptExportService receiptExportService) {
        this.receiptRepository = receiptRepository;
        this.pieceRepository = pieceRepository;
        this.mawbRepository = mawbRepository;
        this.bookingRepository = bookingRepository;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.pdfService = pdfService;
        this.receiptExportService = receiptExportService;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Transactional
    public WarehouseReceipt processWarehouseReceipt(WarehouseReceipt receipt, List<ReceiptPiece> pieces) {
        return processWarehouseReceipt(receipt, pieces, null, false);
    }

    @Transactional
    public WarehouseReceipt updateWarehouseReceipt(UUID receiptId, WarehouseReceipt receipt, List<ReceiptPiece> pieces, List<Map<String, String>> supportingDocs) {
        WarehouseReceipt existing = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Recibo no encontrado: " + receiptId));

        // Auto-resolve airline from MAWB if not provided
        if (receipt.getAirline() == null && receipt.getMawb() != null) {
            mawbRepository.findById(receipt.getMawb().getId()).ifPresent(mawb -> {
                if (mawb.getAirline() != null) receipt.setAirline(mawb.getAirline());
            });
        }

        UUID mawbIdForLock = existing.getMawb() != null ? existing.getMawb().getId() : null;

        synchronized (mawbIdForLock != null ? lockFor(mawbIdForLock) : new Object()) {
            copyReceiptFields(existing, receipt);
            if (supportingDocs != null) {
                try {
                    existing.setSupportingDocs(objectMapper.writeValueAsString(supportingDocs));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Error serializando documentos de soporte", e);
                }
            }

            pieceRepository.deleteByReceiptId(receiptId);
            receiptRepository.flush();
            existing = receiptRepository.save(existing);
        }

        WarehouseReceipt savedReceipt = savePiecesAndRecalculate(existing, pieces);
        recalculateMawbTotals(mawbIdForLock, savedReceipt);

        if (mawbIdForLock != null) {
            syncBookingsAwbNumber(mawbIdForLock);
            List<String> bookingCorrections = recalculateBookingFulfillment(mawbIdForLock);
            savedReceipt.setBookingCorrections(bookingCorrections);
            eventPublisher.publishEvent(new ReceiptCreatedEvent(
                savedReceipt.getId(),
                savedReceipt.getMawb() != null ? savedReceipt.getMawb().getId() : null,
                savedReceipt.getMawb() != null ? savedReceipt.getMawb().getAwbNumber() : null
            ));
        }

        receiptExportService.evictCache(receiptId);
        return savedReceipt;
    }

    @Transactional
    public WarehouseReceipt processWarehouseReceipt(WarehouseReceipt receipt, List<ReceiptPiece> pieces, List<Map<String, String>> supportingDocs) {
        return processWarehouseReceipt(receipt, pieces, supportingDocs, true);
    }

    /**
     * @param purgeExistingForMawb si es true (comportamiento por defecto/historico), borra cualquier
     *                             recibo previo del mismo MAWB antes de insertar el nuevo. Si es false,
     *                             simplemente inserta el nuevo recibo dejando intactos los existentes
     *                             (usado por el flujo de "recibo general + un recibo por HAWB" para que
     *                             cada llamada no destruya la anterior dentro de la misma sesion).
     */
    @Transactional
    public WarehouseReceipt processWarehouseReceipt(WarehouseReceipt receipt, List<ReceiptPiece> pieces, List<Map<String, String>> supportingDocs, boolean purgeExistingForMawb) {
        // Auto-resolve airline from MAWB if not provided
        if (receipt.getAirline() == null && receipt.getMawb() != null) {
            mawbRepository.findById(receipt.getMawb().getId()).ifPresent(mawb -> {
                if (mawb.getAirline() != null) receipt.setAirline(mawb.getAirline());
            });
        }
        if (supportingDocs != null && !supportingDocs.isEmpty()) {
            try {
                receipt.setSupportingDocs(objectMapper.writeValueAsString(supportingDocs));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializando documentos de soporte", e);
            }
        }

        UUID mawbId = receipt.getMawb() != null ? receipt.getMawb().getId() : null;
        UUID hawbId = receipt.getHawbId();

        WarehouseReceipt savedReceipt;
        java.util.List<UUID> evictedReceiptIds = new java.util.ArrayList<>();

        if (mawbId != null && hawbId != null && !purgeExistingForMawb) {
            // HAWB-specific receipt: si ya existe un recibo para este (MAWB, HAWB),
            // actualizarlo en vez de insertar uno nuevo, evitando duplicacion.
            synchronized (lockFor(mawbId)) {
                Optional<WarehouseReceipt> existingOpt = receiptRepository.findByMawbIdAndHawbId(mawbId, hawbId);
                if (existingOpt.isPresent()) {
                    WarehouseReceipt existing = existingOpt.get();
                    copyReceiptFields(existing, receipt);
                    if (supportingDocs != null) {
                        try {
                            existing.setSupportingDocs(objectMapper.writeValueAsString(supportingDocs));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Error serializando documentos de soporte", e);
                        }
                    }
                    evictedReceiptIds.add(existing.getId());
                    pieceRepository.deleteByReceiptId(existing.getId());
                    pieceRepository.flush();
                    savedReceipt = receiptRepository.save(existing);
                } else {
                    savedReceipt = receiptRepository.save(receipt);
                }
            }
        } else if (mawbId != null && purgeExistingForMawb) {
            synchronized (lockFor(mawbId)) {
                List<WarehouseReceipt> existing = receiptRepository.findByMawbId(mawbId);
                for (WarehouseReceipt existingReceipt : existing) {
                    evictedReceiptIds.add(existingReceipt.getId());
                    List<ReceiptPiece> existingPieces = pieceRepository.findByReceiptId(existingReceipt.getId());
                    pieceRepository.deleteAll(existingPieces);
                    receiptRepository.delete(existingReceipt);
                }
                receiptRepository.flush();
                savedReceipt = receiptRepository.save(receipt);
            }
        } else if (mawbId != null) {
            synchronized (lockFor(mawbId)) {
                savedReceipt = receiptRepository.save(receipt);
            }
        } else {
            savedReceipt = receiptRepository.save(receipt);
        }

        savedReceipt = savePiecesAndRecalculate(savedReceipt, pieces);

        if (mawbId != null) {
            recalculateMawbTotals(mawbId, savedReceipt);
            syncBookingsAwbNumber(mawbId);
            List<String> bookingCorrections = recalculateBookingFulfillment(mawbId);
            savedReceipt.setBookingCorrections(bookingCorrections);
        }

        if (!evictedReceiptIds.contains(savedReceipt.getId())) {
            eventPublisher.publishEvent(new ReceiptCreatedEvent(
                savedReceipt.getId(),
                savedReceipt.getMawb() != null ? savedReceipt.getMawb().getId() : null,
                savedReceipt.getMawb() != null ? savedReceipt.getMawb().getAwbNumber() : null
            ));
        }
        receiptExportService.evictCache(savedReceipt.getId());
        for (UUID oldId : evictedReceiptIds) {
            receiptExportService.evictCache(oldId);
        }
        return savedReceipt;
    }

    private WarehouseReceipt savePiecesAndRecalculate(WarehouseReceipt receipt, List<ReceiptPiece> pieces) {
        double dimFactorKg = (receipt.getDimFactorIntl() != null) ? receipt.getDimFactorIntl().doubleValue() : 366.0;
        double dimFactorLbs = (receipt.getDimFactorDom() != null) ? receipt.getDimFactorDom().doubleValue() : 194.0;
        BigDecimal lbsToKgFactor = BigDecimal.valueOf(0.45359237);

        for (ReceiptPiece piece : pieces) {
            piece.setId(null);
            piece.setReceipt(receipt);

            double length = piece.getLengthIn() != null ? piece.getLengthIn().doubleValue() : 0.0;
            double width = piece.getWidthIn() != null ? piece.getWidthIn().doubleValue() : 0.0;
            double height = piece.getHeightIn() != null ? piece.getHeightIn().doubleValue() : 0.0;
            int qty = piece.getPieces() != null && piece.getPieces() > 0 ? piece.getPieces() : 1;

            double vol = length * width * height * qty;
            BigDecimal dimWeightKg = BigDecimal.valueOf(vol / dimFactorKg).setScale(3, RoundingMode.HALF_UP);
            BigDecimal dimWeightLbs = BigDecimal.valueOf(vol / dimFactorLbs).setScale(3, RoundingMode.HALF_UP);
            piece.setDimWeightKg(dimWeightKg);
            piece.setDimWeightLbs(dimWeightLbs);

            if (piece.getScaleWeightLbs() != null && (piece.getScaleWeightKg() == null || piece.getScaleWeightKg().compareTo(BigDecimal.ZERO) == 0)) {
                piece.setScaleWeightKg(piece.getScaleWeightLbs().multiply(lbsToKgFactor).setScale(3, RoundingMode.HALF_UP));
            }

            BigDecimal scaleKg = piece.getScaleWeightKg() != null ? piece.getScaleWeightKg() : BigDecimal.ZERO;
            piece.setChargeableKg(scaleKg.max(dimWeightKg).setScale(3, RoundingMode.HALF_UP));

            BigDecimal swLbs = piece.getScaleWeightLbs() != null ? piece.getScaleWeightLbs() : BigDecimal.ZERO;
            piece.setChargeableLbs(swLbs.max(dimWeightLbs).setScale(3, RoundingMode.HALF_UP));

            pieceRepository.save(piece);
        }

        List<ReceiptPiece> savedPieces = pieceRepository.findByReceiptId(receipt.getId());
        int totalPieceQty = savedPieces.stream()
            .mapToInt(p -> p.getPieces() != null && p.getPieces() > 0 ? p.getPieces() : 1)
            .sum();
        receipt.setPieceCount(totalPieceQty);

        BigDecimal totalActualLbs = savedPieces.stream()
            .map(p -> p.getScaleWeightLbs() != null ? p.getScaleWeightLbs() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalActualKg = savedPieces.stream()
            .map(p -> p.getScaleWeightKg() != null ? p.getScaleWeightKg() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDimKg = savedPieces.stream()
            .map(p -> p.getDimWeightKg() != null ? p.getDimWeightKg() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDimLbs = savedPieces.stream()
            .map(p -> p.getDimWeightLbs() != null ? p.getDimWeightLbs() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        receipt.setActualWeightLbs(totalActualLbs);
        receipt.setActualWeightKg(totalActualKg);
        receipt.setChargeableWeightLbs(totalActualLbs.max(totalDimLbs));
        receipt.setChargeableWeightKg(totalActualKg.max(totalDimKg));

        if (totalActualLbs.compareTo(BigDecimal.ZERO) > 0) {
            receipt.setMawbWeightGreatest(totalActualLbs);
        }
        return receiptRepository.save(receipt);
    }

    private void copyReceiptFields(WarehouseReceipt target, WarehouseReceipt source) {
        if (source.getMawb() != null) {
            target.setMawb(source.getMawb());
        }
        if (source.getHawbId() != null) {
            target.setHawbId(source.getHawbId());
        }
        target.setGatewayCfs(source.getGatewayCfs());
        target.setShipperName(source.getShipperName());
        target.setConsigneeName(source.getConsigneeName());
        target.setOrigin(source.getOrigin());
        target.setDestination(source.getDestination());
        target.setAwbReportedPieces(source.getAwbReportedPieces());
        target.setMawbWeightGreatest(source.getMawbWeightGreatest());
        target.setPieceCount(source.getPieceCount());
        target.setCashOnly(source.getCashOnly());
        target.setBookedInAcoms(source.getBookedInAcoms());
        target.setDocsProvided(source.getDocsProvided());
        target.setCustomsCompleted(source.getCustomsCompleted());
        target.setPreBuilt(source.getPreBuilt());
        target.setRemarks(source.getRemarks());
        target.setDockSignature(source.getDockSignature());
        target.setPrintName(source.getPrintName());
        target.setDeliveredByName(source.getDeliveredByName());
        target.setDeliveredByIdNum(source.getDeliveredByIdNum());
        target.setDeliveredBySigUrl(source.getDeliveredBySigUrl());
        target.setReceivedByName(source.getReceivedByName());
        target.setReceivedBySigUrl(source.getReceivedBySigUrl());
        target.setBrokerName(source.getBrokerName());
        target.setBrokerIdNum(source.getBrokerIdNum());
        target.setBrokerSigUrl(source.getBrokerSigUrl());
        target.setReceiptDate(source.getReceiptDate());
        target.setDimFactorIntl(source.getDimFactorIntl());
        target.setDimFactorDom(source.getDimFactorDom());
        target.setShipperReportedWeight(source.getShipperReportedWeight());
        target.setPdfData(null);
    }

    public String generateSupportingDocsHtml(UUID receiptId) {
        WarehouseReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Recibo no encontrado: " + receiptId));

        String rawDocs = receipt.getSupportingDocs();
        if (rawDocs == null || rawDocs.isBlank() || "[]".equals(rawDocs)) {
            return "<html><body style='font-family:monospace;padding:2rem;color:#333'><h2>Sin evidencias documentales</h2><p>Este recibo no tiene documentos de soporte asociados.</p></body></html>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        sb.append("<title>Evidencias Documentales - ").append(receiptId.toString().substring(0, 8)).append("</title>");
        sb.append("<style>");
        sb.append("*{margin:0;padding:0;box-sizing:border-box}");
        sb.append("body{font-family:'Courier New',monospace;background:#f5f5f5;color:#1a1a1a;padding:2rem}");
        sb.append(".header{max-width:900px;margin:0 auto 2rem;padding:1.5rem;background:#fff;border:1px solid #ddd;border-left:4px solid #1a1a1a}");
        sb.append(".header h1{font-size:1.2rem;text-transform:uppercase;letter-spacing:0.05em}");
        sb.append(".header p{font-size:0.75rem;color:#666;margin-top:0.25rem}");
        sb.append(".grid{max-width:900px;margin:0 auto;display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:1rem}");
        sb.append(".card{background:#fff;border:1px solid #e0e0e0;border-radius:4px;overflow:hidden;break-inside:avoid}");
        sb.append(".card img{width:100%;height:200px;object-fit:cover;display:block;border-bottom:1px solid #eee}");
        sb.append(".card .doc-icon{width:100%;height:120px;display:flex;align-items:center;justify-content:center;background:#fafafa;border-bottom:1px solid #eee;font-size:2.5rem;color:#999}");
        sb.append(".card .info{padding:0.6rem;font-size:0.7rem;color:#555;text-transform:uppercase;letter-spacing:0.03em}");
        sb.append(".footer{max-width:900px;margin:2rem auto 0;padding:1rem 1.5rem;background:#fff;border:1px solid #ddd;font-size:0.65rem;color:#999;text-align:center;text-transform:uppercase;letter-spacing:0.05em}");
        sb.append("@media print{body{background:#fff;padding:0}.header,.card,.footer{border-color:#ccc;box-shadow:none}.card{break-inside:avoid}}");
        sb.append("</style></head><body>");

        String mawbInfo = "";
        if (receipt.getMawb() != null) {
            mawbInfo = "MAWB: " + (receipt.getMawb().getAwbNumber() != null ? receipt.getMawb().getAwbNumber() : "—");
        }
        sb.append("<div class='header'><h1>Evidencias Documentales</h1>");
        sb.append("<p>Recibo: ").append(receiptId.toString().substring(0, 8)).append(" &middot; ").append(mawbInfo);
        sb.append(" &middot; ").append(receipt.getShipperName() != null ? receipt.getShipperName() : "").append("</p></div>");

        sb.append("<div class='grid'>");
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> docs = objectMapper.readValue(rawDocs, List.class);
            for (Map<String, String> doc : docs) {
                String name = doc.getOrDefault("name", "Documento");
                String type = doc.getOrDefault("type", "document");
                String url = doc.getOrDefault("url", "");
                if ("document".equals(type) && url != null && url.startsWith("data:application/pdf")) {
                    String base64Data = url.substring(url.indexOf(',') + 1);
                    List<String> pageImages = pdfService.pdfPagesToDataUris(base64Data);
                    if (!pageImages.isEmpty()) {
                        for (int pi = 0; pi < pageImages.size(); pi++) {
                            sb.append("<div class='card'>");
                            sb.append("<img src='").append(pageImages.get(pi)).append("' alt='").append(name).append("' loading='lazy' />");
                            sb.append("<div class='info'>").append(name).append(" (p\u00e1gina ").append(pi + 1).append(")").append("</div>");
                            sb.append("</div>");
                        }
                    } else {
                        sb.append("<div class='card'>");
                        sb.append("<div class='doc-icon'>&#128196;</div>");
                        sb.append("<div class='info'>").append(name).append("</div>");
                        sb.append("</div>");
                    }
                } else if ("image".equals(type) && url != null && !url.isEmpty()) {
                    sb.append("<div class='card'>");
                    sb.append("<img src='").append(url).append("' alt='").append(name).append("' loading='lazy' />");
                    sb.append("<div class='info'>").append(name).append("</div>");
                    sb.append("</div>");
                } else {
                    sb.append("<div class='card'>");
                    sb.append("<div class='doc-icon'>&#128196;</div>");
                    sb.append("<div class='info'>").append(name).append("</div>");
                    sb.append("</div>");
                }
            }
        } catch (Exception e) {
            sb.append("<p>Error al procesar evidencias</p>");
        }
        sb.append("</div>");
        sb.append("<div class='footer'>AirCargo &mdash; Documento generado el ").append(java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 16)).append("</div>");
        sb.append("</body></html>");
        return sb.toString();
    }

    public byte[] generateSupportingDocsPdf(UUID receiptId) {
        WarehouseReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Recibo no encontrado: " + receiptId));

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'/>");
        sb.append("<meta name='viewport' content='width=device-width,initial-scale=1.0'/>");
        sb.append("<title>Evidencias - ").append(receiptId.toString().substring(0, 8)).append("</title>");
        sb.append("<style>");
        sb.append("@page{margin:1cm}");
        sb.append("body{font-family:'JetBrains Mono',Helvetica,Arial,sans-serif;color:#1a1a1a;font-size:10pt;margin:0;padding:0}");
        sb.append(".page{page-break-after:always;display:flex;flex-direction:column;align-items:center;min-height:100vh;box-sizing:border-box;padding:0.5cm 1cm 1cm}");
        sb.append(".page:last-child{page-break-after:auto}");
        sb.append(".page-header{width:100%;border-bottom:2px solid #333;padding-bottom:0.3cm;margin-bottom:0.5cm;text-align:center}");
        sb.append(".page-header h2{font-size:12pt;margin:0;color:#1a1a1a}");
        sb.append(".page-header .meta{font-size:7pt;color:#555;margin-top:0.15cm}");
        sb.append(".page img{display:block;margin:0 auto;max-width:100%;max-height:75vh;object-fit:contain}");
        sb.append(".page .placeholder{font-size:10pt;color:#999;text-align:center;padding:3cm 1cm}");
        sb.append(".footer-text{font-size:7pt;color:#999;text-align:center;margin-top:auto;padding-top:0.5cm;width:100%;border-top:1px solid #ddd}");
        sb.append(".sig-card{width:100%;border:1px solid #ccc;border-radius:4px;padding:0.4cm 0.5cm;margin-bottom:0.3cm;background:#fafafa}");
        sb.append(".sig-card h3{font-size:10pt;margin:0 0 0.15cm 0;color:#333;border-bottom:1px solid #ddd;padding-bottom:0.1cm}");
        sb.append(".sig-row{display:flex;gap:0.3cm;margin-top:0.2cm}");
        sb.append(".sig-field{flex:1}");
        sb.append(".sig-field label{font-size:7pt;color:#666;text-transform:uppercase;display:block;margin-bottom:0.05cm}");
        sb.append(".sig-field .value{font-size:9pt;color:#1a1a1a;font-weight:bold}");
        sb.append(".sig-img{max-height:1.2cm;max-width:5cm;display:block;margin:0.1cm 0;border:1px solid #eee;padding:0.1cm;background:white}");
        sb.append(".sig-grid{display:grid;grid-template-columns:1fr 1fr;gap:0.4cm;width:100%}");
        sb.append("</style></head><body>");

        String mawbInfo = "";
        String shipperInfo = "";
        if (receipt.getMawb() != null) {
            mawbInfo = xmlEscape(receipt.getMawb().getAwbNumber() != null ? receipt.getMawb().getAwbNumber() : "\u2014");
        }
        if (receipt.getShipperName() != null) {
            shipperInfo = xmlEscape(receipt.getShipperName());
        }

        String footer = "AirCargo \u2014 generado " + java.time.LocalDateTime.now().toString().replace("T", " ").substring(0, 16);

        // ── Page 1: Signature Evidence ──
        sb.append("<div class='page'>");
        sb.append("<div class='page-header'><h2>Documentaci\u00f3n de Evidencias \u2014 Firmas</h2>");
        sb.append("<div class='meta'>MAWB: ").append(mawbInfo).append(" &#183; ").append(shipperInfo).append("</div></div>");

        sb.append("<div class='sig-grid'>");

        // Received by
        sb.append("<div class='sig-card'>");
        sb.append("<h3>Recibido por (Almac\u00e9n)</h3>");
        sb.append("<div class='sig-row'><div class='sig-field'><label>Nombre</label><div class='value'>").append(xmlEscape(receipt.getPrintName() != null ? receipt.getPrintName() : (receipt.getReceivedByName() != null ? receipt.getReceivedByName() : "\u2014"))).append("</div></div></div>");
        if (receipt.getDockSignature() != null && !receipt.getDockSignature().isEmpty()) {
            sb.append("<img class='sig-img' src='").append(receipt.getDockSignature()).append("' alt='Firma Recibido' />");
        } else if (receipt.getReceivedBySigUrl() != null && !receipt.getReceivedBySigUrl().isEmpty()) {
            sb.append("<img class='sig-img' src='").append(receipt.getReceivedBySigUrl()).append("' alt='Firma Recibido' />");
        }
        sb.append("</div>");

        // Delivered by
        sb.append("<div class='sig-card'>");
        sb.append("<h3>Entregado por (Transportista)</h3>");
        sb.append("<div class='sig-row'>");
        sb.append("<div class='sig-field'><label>Nombre</label><div class='value'>").append(xmlEscape(receipt.getDeliveredByName() != null ? receipt.getDeliveredByName() : "\u2014")).append("</div></div>");
        sb.append("<div class='sig-field'><label>ID / C\u00e9dula</label><div class='value'>").append(xmlEscape(receipt.getDeliveredByIdNum() != null ? receipt.getDeliveredByIdNum() : "\u2014")).append("</div></div>");
        sb.append("</div>");
        if (receipt.getDeliveredBySigUrl() != null && !receipt.getDeliveredBySigUrl().isEmpty()) {
            sb.append("<img class='sig-img' src='").append(receipt.getDeliveredBySigUrl()).append("' alt='Firma Entregado' />");
        }
        sb.append("</div>");

        // Broker representative
        sb.append("<div class='sig-card'>");
        sb.append("<h3>Representante de Broker / Agente de Carga</h3>");
        sb.append("<div class='sig-row'>");
        sb.append("<div class='sig-field'><label>Nombre</label><div class='value'>").append(xmlEscape(receipt.getBrokerName() != null ? receipt.getBrokerName() : "\u2014")).append("</div></div>");
        sb.append("<div class='sig-field'><label>ID / C\u00e9dula</label><div class='value'>").append(xmlEscape(receipt.getBrokerIdNum() != null ? receipt.getBrokerIdNum() : "\u2014")).append("</div></div>");
        sb.append("</div>");
        if (receipt.getBrokerSigUrl() != null && !receipt.getBrokerSigUrl().isEmpty()) {
            sb.append("<img class='sig-img' src='").append(receipt.getBrokerSigUrl()).append("' alt='Firma Broker' />");
        }
        sb.append("</div>");

        sb.append("</div>"); // end sig-grid
        sb.append("<div class='footer-text'>").append(footer).append("</div>");
        sb.append("</div>"); // end page

        // ── Evidence pages ──
        String rawDocs = receipt.getSupportingDocs();
        if (rawDocs != null && !rawDocs.isBlank() && !"[]".equals(rawDocs)) {
            try {
                @SuppressWarnings("unchecked")
                List<Map<String, String>> docs = objectMapper.readValue(rawDocs, List.class);
                int idx = 0;
                int total = docs.size();
                for (Map<String, String> doc : docs) {
                    idx++;
                    String name = xmlEscape(doc.getOrDefault("name", "Documento"));
                    String type = doc.getOrDefault("type", "document");
                    String url = doc.getOrDefault("url", "");

                    if ("document".equals(type) && url != null && url.startsWith("data:application/pdf")) {
                        String base64Data = url.substring(url.indexOf(',') + 1);
                        List<String> pageImages = pdfService.pdfPagesToDataUris(base64Data);
                        if (!pageImages.isEmpty()) {
                            for (int pi = 0; pi < pageImages.size(); pi++) {
                                sb.append("<div class='page'>");
                                sb.append("<div class='page-header'><h2>Evidencia ").append(idx).append(" / ").append(total).append(" &#183; ").append(name).append(" (p\u00e1gina ").append(pi + 1).append(")</h2>");
                                sb.append("<div class='meta'>MAWB: ").append(mawbInfo).append("</div></div>");
                                sb.append("<img src='").append(pageImages.get(pi)).append("' alt='").append(name).append("' />");
                                sb.append("<div class='footer-text'>").append(footer).append("</div>");
                                sb.append("</div>");
                            }
                        } else {
                            sb.append("<div class='page'>");
                            sb.append("<div class='page-header'><h2>Evidencia ").append(idx).append(" / ").append(total).append("</h2>");
                            sb.append("<div class='meta'>").append(name).append(" &#183; ").append("MAWB: ").append(mawbInfo).append("</div></div>");
                            sb.append("<div class='placeholder'>&#128196; ").append(name).append("</div>");
                            sb.append("<div class='footer-text'>").append(footer).append("</div>");
                            sb.append("</div>");
                        }
                    } else if ("image".equals(type) && url != null && !url.isEmpty()) {
                        sb.append("<div class='page'>");
                        sb.append("<div class='page-header'><h2>Evidencia ").append(idx).append(" / ").append(total).append("</h2>");
                        sb.append("<div class='meta'>").append(name).append(" &#183; ").append("MAWB: ").append(mawbInfo).append("</div></div>");
                        sb.append("<img src='").append(url).append("' alt='").append(name).append("' />");
                        sb.append("<div class='footer-text'>").append(footer).append("</div>");
                        sb.append("</div>");
                    } else {
                        sb.append("<div class='page'>");
                        sb.append("<div class='page-header'><h2>Evidencia ").append(idx).append(" / ").append(total).append("</h2>");
                        sb.append("<div class='meta'>").append(name).append(" &#183; ").append("MAWB: ").append(mawbInfo).append("</div></div>");
                        sb.append("<div class='placeholder'>&#128196; ").append(name).append("</div>");
                        sb.append("<div class='footer-text'>").append(footer).append("</div>");
                        sb.append("</div>");
                    }
                }
            } catch (Exception e) {
                sb.append("<p>Error al procesar evidencias</p>");
            }
        } else {
            sb.append("<div class='page'><div class='page-header'><h2>Evidencias</h2></div>");
            sb.append("<div class='placeholder'>Sin evidencias documentales adicionales</div>");
            sb.append("<div class='footer-text'>").append(footer).append("</div></div>");
        }

        sb.append("</body></html>");
        return pdfService.generatePdf(sb.toString());
    }

    private static String xmlEscape(String s) {
        return com.aircargo.common.util.TextUtil.xmlEscape(s);
    }
}
