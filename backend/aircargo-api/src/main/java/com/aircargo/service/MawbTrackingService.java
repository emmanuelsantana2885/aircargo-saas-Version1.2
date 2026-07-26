package com.aircargo.service;

import com.aircargo.entity.*;
import com.aircargo.repository.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MawbTrackingService {

    private final MawbRepository mawbRepository;
    private final BookingRepository bookingRepository;
    private final WarehouseReceiptRepository receiptRepository;
    private final UldAwbRepository uldAwbRepository;
    private final DuaRecordRepository duaRecordRepository;

    public MawbTrackingService(MawbRepository mawbRepository, BookingRepository bookingRepository,
                                WarehouseReceiptRepository receiptRepository, UldAwbRepository uldAwbRepository,
                                DuaRecordRepository duaRecordRepository) {
        this.mawbRepository = mawbRepository;
        this.bookingRepository = bookingRepository;
        this.receiptRepository = receiptRepository;
        this.uldAwbRepository = uldAwbRepository;
        this.duaRecordRepository = duaRecordRepository;
    }

    public List<Map<String, Object>> getMawbTimeline(UUID mawbId) {
        Mawb mawb = mawbRepository.findById(mawbId)
                .orElseThrow(() -> new IllegalArgumentException("MAWB no encontrado: " + mawbId));
        List<Map<String, Object>> events = new ArrayList<>();

        // 1. MAWB created
        events.add(event("MAWB Creado", "El conocimiento aereo fue registrado en el sistema",
                mawb.getCreatedAt(), "CREADO", mawb.getAwbNumber()));

        // 2. Booking
        List<Booking> bookings = bookingRepository.findByMawbId(mawbId);
        for (Booking b : bookings) {
            events.add(event("Booking Registrado", "Reserva creada para " + b.getSkids() + " piezas",
                    b.getCreatedAt(), "RESERVA", b.getAwbNumber()));
        }

        // 3. Warehouse Receipts (only active)
        List<WarehouseReceipt> receipts = receiptRepository.findByMawbIdAndSupersededFalse(mawbId);
        for (WarehouseReceipt r : receipts) {
            events.add(event("Recibo de Bodega", "Recibo emitido: " + r.getPieceCount() + " piezas, " +
                    (r.getActualWeightKg() != null ? r.getActualWeightKg() + " kg" : "0 kg"),
                    r.getCreatedAt(), "RECIBO", r.getMawb() != null ? r.getMawb().getAwbNumber() : ""));
        }

        // 4. ULD assignments
        List<UldAwb> uldAwbs = uldAwbRepository.findByMawbId(mawbId);
        for (UldAwb ua : uldAwbs) {
            String uldNum = ua.getUld() != null ? ua.getUld().getUldNumber() : "";
            events.add(event("Asignado a ULD", "Carga asignada al contenedor " + uldNum +
                    " (" + ua.getPieces() + " piezas)", ua.getCreatedAt(), "ULD", uldNum));
        }

        // 5. DUA records
        List<DuaRecord> duas = duaRecordRepository.findByMawbIdOrderByCreatedAtDesc(mawbId);
        for (DuaRecord d : duas) {
            events.add(event("DUA " + d.getStatus().name(), "DUA #" + d.getDuaNumber() +
                    (d.getCustomsBroker() != null ? " — " + d.getCustomsBroker() : ""),
                    d.getCreatedAt(), "DUA", d.getDuaNumber()));
        }

        events.sort(Comparator.comparing(m -> (OffsetDateTime) m.get("timestamp")));
        return events;
    }

    public List<Map<String, Object>> getAllMawbTrackingSummary() {
        List<Mawb> allMawbs = mawbRepository.findAll();
        return allMawbs.stream().map(m -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("awbNumber", m.getAwbNumber());
            item.put("shipperName", m.getShipperName());
            item.put("origin", m.getOrigin());
            item.put("destination", m.getDestination());
            item.put("pieces", m.getPieces());
            item.put("status", m.getStatus() != null ? m.getStatus().name() : "BOOKED");
            item.put("createdAt", m.getCreatedAt());
            List<WarehouseReceipt> receipts = receiptRepository.findByMawbIdAndSupersededFalse(m.getId());
            boolean hasReceipt = !receipts.isEmpty();
            item.put("hasReceipt", hasReceipt);
            List<UldAwb> uldLinks = uldAwbRepository.findByMawbId(m.getId());
            item.put("uldCount", uldLinks.size());
            List<DuaRecord> duas = duaRecordRepository.findByMawbIdOrderByCreatedAtDesc(m.getId());
            item.put("duaCount", duas.size());
            boolean duaComplete = duas.stream().anyMatch(d -> d.getStatus() == DuaStatus.COMPLETED);
            item.put("duaComplete", duaComplete);
            return item;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> event(String title, String description, OffsetDateTime timestamp,
                                       String type, String reference) {
        Map<String, Object> e = new LinkedHashMap<>();
        e.put("title", title);
        e.put("description", description);
        e.put("timestamp", timestamp);
        e.put("type", type);
        e.put("reference", reference);
        return e;
    }
}
