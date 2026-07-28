package com.aircargo.exportservice.service;

import com.aircargo.exportservice.entity.*;
import com.aircargo.exportservice.repository.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExportService {

    private final MawbRepository mawbRepository;
    private final BookingRepository bookingRepository;
    private final WarehouseReceiptRepository receiptRepository;
    private final FlightRepository flightRepository;
    private final UldRepository uldRepository;
    private final HawbRepository hawbRepository;
    private final AuditLogRepository auditLogRepository;
    private final AppUserRepository appUserRepository;

    public ExportService(MawbRepository mawbRepository, BookingRepository bookingRepository,
                          WarehouseReceiptRepository receiptRepository, FlightRepository flightRepository,
                          UldRepository uldRepository, HawbRepository hawbRepository,
                          AuditLogRepository auditLogRepository,
                          AppUserRepository appUserRepository) {
        this.mawbRepository = mawbRepository;
        this.bookingRepository = bookingRepository;
        this.receiptRepository = receiptRepository;
        this.flightRepository = flightRepository;
        this.uldRepository = uldRepository;
        this.hawbRepository = hawbRepository;
        this.auditLogRepository = auditLogRepository;
        this.appUserRepository = appUserRepository;
    }

    private Map<UUID, String> buildUserRoleMap() {
        Map<UUID, String> map = new HashMap<>();
        appUserRepository.findAll().forEach(u -> map.put(u.getId(), u.getRole()));
        return map;
    }

    private String userRole(AuditLogEntity log, Map<UUID, String> roleMap) {
        return log.getUserId() != null ? roleMap.getOrDefault(log.getUserId(), "") : "";
    }

    public ByteArrayInputStream export(String type, String format, LocalDate dateFrom, LocalDate dateTo, boolean audit) {
        return exportCsv(type, dateFrom, dateTo, audit);
    }

    public ByteArrayInputStream exportCsvPublic(String type, String format, LocalDate dateFrom, LocalDate dateTo, boolean audit) {
        return exportCsv(type, dateFrom, dateTo, audit);
    }

    public List<Map<String, Object>> exportJson(String type, LocalDate dateFrom, LocalDate dateTo, boolean audit) {
        return switch (type.toUpperCase()) {
            case "MAWBS" -> audit ? auditJson("MAWB", mawbRepository.findAll().stream().map(m -> m.getId().toString()).toList())
                                  : mawbRepository.findAll().stream().map(this::mawbToMap).collect(Collectors.toList());
            case "BOOKINGS" -> audit ? auditJson("BOOKING", bookingRepository.findAll().stream().map(b -> b.getId().toString()).toList())
                                     : bookingRepository.findAll().stream().map(this::bookingToMap).collect(Collectors.toList());
            case "RECEIPTS" -> audit ? receiptAuditJson(receiptRepository.findAll().stream().map(r -> r.getId().toString()).toList())
                                     : receiptRepository.findAll().stream().map(this::receiptToMap).collect(Collectors.toList());
            case "FLIGHTS" -> audit ? auditJson("FLIGHT", flightRepository.findAll().stream().map(f -> f.getId().toString()).toList())
                                    : flightRepository.findAll().stream().map(this::flightToMap).collect(Collectors.toList());
            case "ULDS" -> audit ? auditJson("ULD", uldRepository.findAll().stream().map(u -> u.getId().toString()).toList())
                                 : uldRepository.findAll().stream().map(this::uldToMap).collect(Collectors.toList());
            case "HAWBS" -> audit ? auditJson("HAWB", hawbRepository.findAll().stream().map(h -> h.getId().toString()).toList())
                                  : hawbRepository.findAll().stream().map(this::hawbToMap).collect(Collectors.toList());
            default -> List.of(Map.of("error", "Tipo no soportado: " + type));
        };
    }

    private ByteArrayInputStream exportCsv(String type, LocalDate dateFrom, LocalDate dateTo, boolean audit) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        if (audit) {
            switch (type.toUpperCase()) {
                case "MAWBS" -> exportAuditMawbs(pw);
                case "BOOKINGS" -> exportAuditBookings(pw);
                case "RECEIPTS" -> exportAuditReceipts(pw);
                case "FLIGHTS" -> exportAuditFlights(pw);
                case "ULDS" -> exportAuditUlds(pw);
                case "HAWBS" -> exportAuditHawbs(pw);
                default -> pw.println("Tipo no soportado: " + type);
            }
        } else {
            switch (type.toUpperCase()) {
                case "MAWBS" -> exportMawbs(pw);
                case "BOOKINGS" -> exportBookings(pw);
                case "RECEIPTS" -> exportReceipts(pw);
                case "FLIGHTS" -> exportFlights(pw);
                case "ULDS" -> exportUlds(pw);
                case "HAWBS" -> exportHawbs(pw);
                default -> pw.println("Tipo no soportado: " + type);
            }
        }
        pw.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    private void exportMawbs(PrintWriter pw) {
        pw.println("AWB Number,Shipper,Consignee,Origin,Destination,Pieces,Reported Weight Kg,Chargeable Weight Kg,Status,Created At");
        for (MawbEntity m : mawbRepository.findAll()) {
            pw.println(join(m.getAwbNumber(), m.getShipperName(), m.getConsigneeName(),
                m.getOrigin(), m.getDestination(), String.valueOf(m.getPieces()),
                str(m.getReportedWeightKg()), str(m.getChargeableWeightKg()),
                m.getStatus() != null ? m.getStatus() : "",
                m.getCreatedAt() != null ? m.getCreatedAt().toString() : ""));
        }
    }

    private void exportBookings(PrintWriter pw) {
        pw.println("ID,AWB Number,Shipper,Destination,Skids,Reserved Kg,Created At");
        for (BookingEntity b : bookingRepository.findAll()) {
            pw.println(join(b.getId().toString(), b.getAwbNumber(), b.getShipperName(),
                b.getDestination(), String.valueOf(b.getSkids()), str(b.getReservedKg()),
                b.getCreatedAt() != null ? b.getCreatedAt().toString() : ""));
        }
    }

    private void exportReceipts(PrintWriter pw) {
        pw.println("ID,MAWB,Shipper,Pieces,Actual Kg,Chargeable Kg,Created At");
        List<MawbEntity> allMawbs = mawbRepository.findAll();
        Map<UUID, String> mawbNumberMap = allMawbs.stream()
                .collect(Collectors.toMap(MawbEntity::getId, MawbEntity::getAwbNumber, (a, b) -> a));
        for (WarehouseReceiptEntity r : receiptRepository.findAll()) {
            String mawbNum = mawbNumberMap.getOrDefault(r.getMawbId(), "");
            pw.println(join(r.getId().toString(), mawbNum, r.getShipperName(),
                String.valueOf(r.getPieceCount()), str(r.getActualWeightKg()),
                str(r.getChargeableWeightKg()),
                r.getCreatedAt() != null ? r.getCreatedAt().toString() : ""));
        }
    }

    private void exportFlights(PrintWriter pw) {
        pw.println("Flight Number,Origin,Destination,Flight Date,Aircraft,Status,Created At");
        for (FlightEntity f : flightRepository.findAll()) {
            pw.println(join(f.getFlightNumber(), f.getOrigin(), f.getDestination(),
                f.getFlightDate() != null ? f.getFlightDate().toString() : "",
                f.getAircraftType() != null ? f.getAircraftType() : "",
                f.getStatus() != null ? f.getStatus() : "",
                f.getCreatedAt() != null ? f.getCreatedAt().toString() : ""));
        }
    }

    private void exportUlds(PrintWriter pw) {
        pw.println("ULD Number,Type,Tare Lbs,Status,Created At");
        for (UldEntity u : uldRepository.findAll()) {
            pw.println(join(u.getUldNumber(), u.getUldType() != null ? u.getUldType() : "",
                str(u.getTareLbs()), u.getStatus() != null ? u.getStatus() : "",
                u.getCreatedAt() != null ? u.getCreatedAt().toString() : ""));
        }
    }

    private void exportHawbs(PrintWriter pw) {
        pw.println("HAWB Number,MAWB,Consignee,Pieces,Weight Kg,Destination,Created At");
        List<MawbEntity> allMawbs = mawbRepository.findAll();
        Map<UUID, String> mawbNumberMap = allMawbs.stream()
                .collect(Collectors.toMap(MawbEntity::getId, MawbEntity::getAwbNumber, (a, b) -> a));
        for (HawbEntity h : hawbRepository.findAll()) {
            String mawbNum = mawbNumberMap.getOrDefault(h.getMawbId(), "");
            pw.println(join(h.getHawbNumber(), mawbNum, h.getConsigneeName(),
                String.valueOf(h.getPieces()), str(h.getWeightKg()), h.getDestination(),
                h.getCreatedAt() != null ? h.getCreatedAt().toString() : ""));
        }
    }

    private void exportAuditMawbs(PrintWriter pw) {
        Map<UUID, String> roleMap = buildUserRoleMap();
        pw.println("AWB Number,shipper,consignee,origin,destination,pieces,reported weight kg,chargeable weight kg,status,created at,created by,transaction type,user role,email");
        for (MawbEntity m : mawbRepository.findAll()) {
            String id = m.getId().toString();
            List<AuditLogEntity> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("MAWB", id);
            for (AuditLogEntity log : logs) {
                pw.println(join(m.getAwbNumber(),
                    m.getShipperName(), m.getConsigneeName(),
                    m.getOrigin(), m.getDestination(), String.valueOf(m.getPieces()),
                    str(m.getReportedWeightKg()), str(m.getChargeableWeightKg()),
                    m.getStatus() != null ? m.getStatus() : "",
                    log.getCreatedAt() != null ? log.getCreatedAt().toString() : "",
                    log.getFullName(), log.getAction(), userRole(log, roleMap), log.getEmail()));
            }
        }
    }

    private void exportAuditBookings(PrintWriter pw) {
        Map<UUID, String> roleMap = buildUserRoleMap();
        pw.println("ID,AWB Number,shipper,destination,skids,reserved kg,created at,created by,transaction type,user role,email");
        for (BookingEntity b : bookingRepository.findAll()) {
            String id = b.getId().toString();
            List<AuditLogEntity> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("BOOKING", id);
            for (AuditLogEntity log : logs) {
                pw.println(join(id, b.getAwbNumber(), b.getShipperName(),
                    b.getDestination(), String.valueOf(b.getSkids()), str(b.getReservedKg()),
                    log.getCreatedAt() != null ? log.getCreatedAt().toString() : "",
                    log.getFullName(), log.getAction(), userRole(log, roleMap), log.getEmail()));
            }
        }
    }

    private void exportAuditReceipts(PrintWriter pw) {
        Map<UUID, String> roleMap = buildUserRoleMap();
        List<MawbEntity> allMawbs = mawbRepository.findAll();
        Map<UUID, String> mawbNumberMap = allMawbs.stream()
                .collect(Collectors.toMap(MawbEntity::getId, MawbEntity::getAwbNumber, (a, b) -> a));
        pw.println("ID,mawb,shipper,pieces,actual kg,chargeable kg,created at,created by,transaction type,details,user role,email");
        for (WarehouseReceiptEntity r : receiptRepository.findAll()) {
            String id = r.getId().toString();
            String mawbNum = mawbNumberMap.getOrDefault(r.getMawbId(), "");
            List<AuditLogEntity> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("RECEIPT", id);
            List<AuditLogEntity> correctionLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("RECEIPT_CORRECTION", id);
            List<AuditLogEntity> allLogs = new ArrayList<>(logs);
            allLogs.addAll(correctionLogs);
            allLogs.sort(Comparator.comparing(AuditLogEntity::getCreatedAt).reversed());
            for (AuditLogEntity log : allLogs) {
                pw.println(join(id, mawbNum, r.getShipperName(),
                    String.valueOf(r.getPieceCount()), str(r.getActualWeightKg()),
                    str(r.getChargeableWeightKg()),
                    log.getCreatedAt() != null ? log.getCreatedAt().toString() : "",
                    log.getFullName(), log.getAction(), log.getDetails(), userRole(log, roleMap), log.getEmail()));
            }
        }
    }

    private void exportAuditFlights(PrintWriter pw) {
        Map<UUID, String> roleMap = buildUserRoleMap();
        pw.println("Flight Number,origin,destination,flight date,aircraft,status,created at,created by,transaction type,user role,email");
        for (FlightEntity f : flightRepository.findAll()) {
            String id = f.getId().toString();
            List<AuditLogEntity> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("FLIGHT", id);
            for (AuditLogEntity log : logs) {
                pw.println(join(f.getFlightNumber(), f.getOrigin(), f.getDestination(),
                    f.getFlightDate() != null ? f.getFlightDate().toString() : "",
                    f.getAircraftType() != null ? f.getAircraftType() : "",
                    f.getStatus() != null ? f.getStatus() : "",
                    log.getCreatedAt() != null ? log.getCreatedAt().toString() : "",
                    log.getFullName(), log.getAction(), userRole(log, roleMap), log.getEmail()));
            }
        }
    }

    private void exportAuditUlds(PrintWriter pw) {
        Map<UUID, String> roleMap = buildUserRoleMap();
        pw.println("ULD Number,type,tare lbs,status,created at,created by,transaction type,user role,email");
        for (UldEntity u : uldRepository.findAll()) {
            String id = u.getId().toString();
            List<AuditLogEntity> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("ULD", id);
            for (AuditLogEntity log : logs) {
                pw.println(join(u.getUldNumber(),
                    u.getUldType() != null ? u.getUldType() : "",
                    str(u.getTareLbs()), u.getStatus() != null ? u.getStatus() : "",
                    log.getCreatedAt() != null ? log.getCreatedAt().toString() : "",
                    log.getFullName(), log.getAction(), userRole(log, roleMap), log.getEmail()));
            }
        }
    }

    private void exportAuditHawbs(PrintWriter pw) {
        Map<UUID, String> roleMap = buildUserRoleMap();
        List<MawbEntity> allMawbs = mawbRepository.findAll();
        Map<UUID, String> mawbNumberMap = allMawbs.stream()
                .collect(Collectors.toMap(MawbEntity::getId, MawbEntity::getAwbNumber, (a, b) -> a));
        pw.println("HAWB Number,mawb,consignee,pieces,weight kg,destination,created at,created by,transaction type,user role,email");
        for (HawbEntity h : hawbRepository.findAll()) {
            String id = h.getId().toString();
            String mawbNum = mawbNumberMap.getOrDefault(h.getMawbId(), "");
            List<AuditLogEntity> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("HAWB", id);
            for (AuditLogEntity log : logs) {
                pw.println(join(h.getHawbNumber(), mawbNum, h.getConsigneeName(),
                    String.valueOf(h.getPieces()), str(h.getWeightKg()), h.getDestination(),
                    log.getCreatedAt() != null ? log.getCreatedAt().toString() : "",
                    log.getFullName(), log.getAction(), userRole(log, roleMap), log.getEmail()));
            }
        }
    }

    private Map<String, Object> mawbToMap(MawbEntity m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("awbNumber", m.getAwbNumber());
        map.put("shipper", m.getShipperName());
        map.put("consignee", m.getConsigneeName());
        map.put("origin", m.getOrigin());
        map.put("destination", m.getDestination());
        map.put("pieces", m.getPieces());
        map.put("reportedWeightKg", m.getReportedWeightKg());
        map.put("chargeableWeightKg", m.getChargeableWeightKg());
        map.put("status", m.getStatus() != null ? m.getStatus() : "");
        map.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : "");
        return map;
    }

    private Map<String, Object> bookingToMap(BookingEntity b) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", b.getId().toString());
        map.put("awbNumber", b.getAwbNumber());
        map.put("shipper", b.getShipperName());
        map.put("destination", b.getDestination());
        map.put("skids", b.getSkids());
        map.put("reservedKg", b.getReservedKg());
        map.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : "");
        return map;
    }

    private Map<String, Object> receiptToMap(WarehouseReceiptEntity r) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId().toString());
        map.put("mawb", r.getMawbId().toString());
        map.put("shipper", r.getShipperName());
        map.put("pieces", r.getPieceCount());
        map.put("actualKg", r.getActualWeightKg());
        map.put("chargeableKg", r.getChargeableWeightKg());
        map.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : "");
        return map;
    }

    private Map<String, Object> flightToMap(FlightEntity f) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("flightNumber", f.getFlightNumber());
        map.put("origin", f.getOrigin());
        map.put("destination", f.getDestination());
        map.put("flightDate", f.getFlightDate() != null ? f.getFlightDate().toString() : "");
        map.put("aircraft", f.getAircraftType() != null ? f.getAircraftType() : "");
        map.put("status", f.getStatus() != null ? f.getStatus() : "");
        map.put("createdAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : "");
        return map;
    }

    private Map<String, Object> uldToMap(UldEntity u) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("uldNumber", u.getUldNumber());
        map.put("type", u.getUldType() != null ? u.getUldType() : "");
        map.put("tareLbs", u.getTareLbs());
        map.put("status", u.getStatus() != null ? u.getStatus() : "");
        map.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
        return map;
    }

    private Map<String, Object> hawbToMap(HawbEntity h) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("hawbNumber", h.getHawbNumber());
        map.put("mawb", h.getMawbId().toString());
        map.put("consignee", h.getConsigneeName());
        map.put("pieces", h.getPieces());
        map.put("weightKg", h.getWeightKg());
        map.put("destination", h.getDestination());
        map.put("createdAt", h.getCreatedAt() != null ? h.getCreatedAt().toString() : "");
        return map;
    }

    private List<Map<String, Object>> auditJson(String entityType, List<String> ids) {
        Map<UUID, String> roleMap = buildUserRoleMap();
        List<Map<String, Object>> result = new ArrayList<>();
        for (String id : ids) {
            List<AuditLogEntity> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, id);
            for (AuditLogEntity log : logs) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("entityId", id);
                m.put("transactionType", log.getAction());
                m.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().toString() : "");
                m.put("createdBy", log.getFullName());
                m.put("userRole", userRole(log, roleMap));
                m.put("email", log.getEmail());
                result.add(m);
            }
        }
        return result;
    }

    private List<Map<String, Object>> receiptAuditJson(List<String> ids) {
        Map<UUID, String> roleMap = buildUserRoleMap();
        List<Map<String, Object>> result = new ArrayList<>();
        for (String id : ids) {
            List<AuditLogEntity> logs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("RECEIPT", id);
            List<AuditLogEntity> correctionLogs = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("RECEIPT_CORRECTION", id);
            List<AuditLogEntity> allLogs = new ArrayList<>(logs);
            allLogs.addAll(correctionLogs);
            allLogs.sort(Comparator.comparing(AuditLogEntity::getCreatedAt).reversed());
            for (AuditLogEntity log : allLogs) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("entityId", id);
                m.put("transactionType", log.getAction());
                m.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().toString() : "");
                m.put("createdBy", log.getFullName());
                m.put("userRole", userRole(log, roleMap));
                m.put("email", log.getEmail());
                result.add(m);
            }
        }
        return result;
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
    }

    private String join(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            String f = fields[i] != null ? fields[i] : "";
            if (f.contains(",") || f.contains("\"") || f.contains("\n")) {
                f = "\"" + f.replace("\"", "\"\"") + "\"";
            }
            sb.append(f);
            if (i < fields.length - 1) sb.append(",");
        }
        return sb.toString();
    }
}
