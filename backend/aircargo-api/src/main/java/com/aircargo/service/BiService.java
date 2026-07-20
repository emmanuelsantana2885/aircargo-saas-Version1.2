package com.aircargo.service;

import com.aircargo.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BiService {

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final MawbRepository mawbRepository;
    private final UldRepository uldRepository;
    private final UldAwbRepository uldAwbRepository;
    private final WarehouseReceiptRepository receiptRepository;
    private final DuaRecordRepository duaRecordRepository;

    public BiService(FlightRepository flightRepository,
                     BookingRepository bookingRepository,
                     MawbRepository mawbRepository,
                     UldRepository uldRepository,
                     UldAwbRepository uldAwbRepository,
                     WarehouseReceiptRepository receiptRepository,
                     DuaRecordRepository duaRecordRepository) {
        this.flightRepository = flightRepository;
        this.bookingRepository = bookingRepository;
        this.mawbRepository = mawbRepository;
        this.uldRepository = uldRepository;
        this.uldAwbRepository = uldAwbRepository;
        this.receiptRepository = receiptRepository;
        this.duaRecordRepository = duaRecordRepository;
    }

    public List<Map<String, Object>> getFlights(LocalDate dateFrom, LocalDate dateTo) {
        List<com.aircargo.entity.Flight> flights = flightRepository.findAll();
        if (dateFrom != null) {
            flights = flights.stream()
                    .filter(f -> !f.getFlightDate().isBefore(dateFrom))
                    .collect(Collectors.toList());
        }
        if (dateTo != null) {
            flights = flights.stream()
                    .filter(f -> !f.getFlightDate().isAfter(dateTo))
                    .collect(Collectors.toList());
        }

        return flights.stream().map(f -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("flightId", f.getId());
            row.put("flightNumber", f.getFlightNumber());
            row.put("airlineCode", f.getAirline() != null ? f.getAirline().getCode() : null);
            row.put("airlineName", f.getAirline() != null ? f.getAirline().getName() : null);
            row.put("origin", f.getOrigin());
            row.put("destination", f.getDestination());
            row.put("flightDate", f.getFlightDate() != null ? f.getFlightDate().toString() : null);
            row.put("aircraftType", f.getAircraftType() != null ? f.getAircraftType().name() : null);
            row.put("aircraftReg", f.getAircraftReg());
            row.put("status", f.getStatus() != null ? f.getStatus().name() : null);
            row.put("maxPayloadKg", f.getMaxPayloadKg());

            List<com.aircargo.entity.Uld> ulds = uldRepository.findByFlightId(f.getId());
            row.put("totalUlds", ulds.size());

            BigDecimal totalGrossLbs = ulds.stream()
                    .map(u -> u.getGrossWeightLbs() != null ? u.getGrossWeightLbs() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            row.put("totalGrossWeightLbs", totalGrossLbs);

            BigDecimal totalNetLbs = ulds.stream()
                    .map(u -> u.getNetWeightLbs() != null ? u.getNetWeightLbs() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            row.put("totalNetWeightLbs", totalNetLbs);

            List<com.aircargo.entity.Booking> bookings = bookingRepository.findByFlightId(f.getId());
            row.put("bookingsCount", bookings.size());

            int totalPieces = uldAwbRepository.findByUldIdIn(
                    ulds.stream().map(com.aircargo.entity.Uld::getId).collect(Collectors.toList())
            ).stream()
                    .mapToInt(a -> a.getPieces() != null ? a.getPieces() : 0)
                    .sum();
            row.put("totalPieces", totalPieces);

            return row;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getBookings(LocalDate dateFrom, LocalDate dateTo) {
        List<com.aircargo.entity.Booking> bookings = bookingRepository.findAll();
        if (dateFrom != null || dateTo != null) {
            bookings = bookings.stream()
                    .filter(b -> {
                        java.time.LocalDate bDate = b.getFlight() != null ? b.getFlight().getFlightDate() : null;
                        if (bDate == null) return false;
                        if (dateFrom != null && bDate.isBefore(dateFrom)) return false;
                        if (dateTo != null && bDate.isAfter(dateTo)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
        }
        return bookings.stream().map(b -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("bookingId", b.getId());
            row.put("awbNumber", b.getAwbNumber());
            row.put("clientName", b.getClientName());
            row.put("shipperName", b.getShipperName());
            row.put("consignee", b.getCnee());
            row.put("airlineCode", b.getAirline() != null ? b.getAirline().getCode() : null);
            row.put("airlineName", b.getAirline() != null ? b.getAirline().getName() : null);
            row.put("flightNumber", b.getFlight() != null ? b.getFlight().getFlightNumber() : null);
            row.put("flightDate", b.getFlight() != null && b.getFlight().getFlightDate() != null
                    ? b.getFlight().getFlightDate().toString() : null);
            row.put("destination", b.getDestination());
            row.put("skids", b.getSkids());
            row.put("units", b.getUnits());
            row.put("reservedKg", b.getReservedKg());
            row.put("confirmedKg", b.getConfirmedKg());
            row.put("receivedKg", b.getReceivedKg());
            row.put("fulfillmentPct", b.getFulfillmentPct());
            row.put("commodityType", b.getCommodityType() != null ? b.getCommodityType().name() : null);
            row.put("priority", b.getPriority());
            row.put("isConfirmed", b.getIsConfirmed());
            row.put("mawbStatus", b.getMawb() != null && b.getMawb().getStatus() != null
                    ? b.getMawb().getStatus().name() : null);
            row.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : null);
            return row;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMawbs() {
        List<com.aircargo.entity.Mawb> mawbs = mawbRepository.findAll();
        return mawbs.stream().map(m -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("mawbId", m.getId());
            row.put("awbNumber", m.getAwbNumber());
            row.put("airlineCode", m.getAirline() != null ? m.getAirline().getCode() : null);
            row.put("airlineName", m.getAirline() != null ? m.getAirline().getName() : null);
            row.put("flightNumber", m.getFlight() != null ? m.getFlight().getFlightNumber() : null);
            row.put("flightDate", m.getFlight() != null && m.getFlight().getFlightDate() != null
                    ? m.getFlight().getFlightDate().toString() : null);
            row.put("shipperName", m.getShipperName());
            row.put("consigneeName", m.getConsigneeName());
            row.put("origin", m.getOrigin());
            row.put("destination", m.getDestination());
            row.put("pieces", m.getPieces());
            row.put("reportedWeightKg", m.getReportedWeightKg());
            row.put("chargeableWeightKg", m.getChargeableWeightKg());
            row.put("commodityType", m.getCommodityType() != null ? m.getCommodityType().name() : null);
            row.put("status", m.getStatus() != null ? m.getStatus().name() : null);
            row.put("cashOnly", m.getCashOnly());
            row.put("preBuilt", m.getPreBuilt());

            List<com.aircargo.entity.UldAwb> uldAwbs = uldAwbRepository.findByMawbId(m.getId());
            row.put("uldCount", uldAwbs.stream()
                    .map(a -> a.getUld() != null ? a.getUld().getId() : null)
                    .filter(Objects::nonNull)
                    .distinct().count());

            int dispatchedPieces = uldAwbs.stream()
                    .mapToInt(a -> a.getPieces() != null ? a.getPieces() : 0)
                    .sum();
            row.put("dispatchedPieces", dispatchedPieces);

            boolean hasReceipt = receiptRepository.findByMawbId(m.getId()).stream().findFirst().isPresent();
            row.put("hasReceipt", hasReceipt);
            row.put("hasDua", duaRecordRepository.existsByMawbId(m.getId()));
            row.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
            row.put("updatedAt", m.getUpdatedAt() != null ? m.getUpdatedAt().toString() : null);
            return row;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getReceipts() {
        List<com.aircargo.entity.WarehouseReceipt> receipts = receiptRepository.findAll();
        return receipts.stream().map(r -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("receiptId", r.getId());
            row.put("awbNumber", r.getMawb() != null ? r.getMawb().getAwbNumber() : null);
            row.put("airlineCode", r.getAirline() != null ? r.getAirline().getCode() : null);
            row.put("airlineName", r.getAirline() != null ? r.getAirline().getName() : null);
            row.put("shipperName", r.getShipperName());
            row.put("consigneeName", r.getConsigneeName());
            row.put("origin", r.getOrigin());
            row.put("destination", r.getDestination());
            row.put("pieceCount", r.getPieceCount());
            row.put("awbReportedPieces", r.getAwbReportedPieces());
            row.put("actualWeightKg", r.getActualWeightKg());
            row.put("chargeableWeightKg", r.getChargeableWeightKg());
            row.put("actualWeightLbs", r.getActualWeightLbs());
            row.put("chargeableWeightLbs", r.getChargeableWeightLbs());
            row.put("cashOnly", r.getCashOnly());
            row.put("bookedInAcoms", r.getBookedInAcoms());
            row.put("docsProvided", r.getDocsProvided());
            row.put("customsCompleted", r.getCustomsCompleted());
            row.put("preBuilt", r.getPreBuilt());
            row.put("createdByName", r.getCreatedByName());
            row.put("receiptDate", r.getReceiptDate() != null ? r.getReceiptDate().toString() : null);
            row.put("createdAt", r.getCreatedAt() != null ? r.getCreatedAt().toString() : null);
            return row;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getUlds() {
        List<com.aircargo.entity.Uld> ulds = uldRepository.findAll();
        return ulds.stream().map(u -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("uldId", u.getId());
            row.put("uldNumber", u.getUldNumber());
            row.put("uldType", u.getUldType() != null ? u.getUldType().name() : null);
            row.put("airlineCode", u.getAirline() != null ? u.getAirline().getCode() : null);
            row.put("airlineName", u.getAirline() != null ? u.getAirline().getName() : null);
            row.put("flightNumber", u.getFlight() != null ? u.getFlight().getFlightNumber() : null);
            row.put("flightDate", u.getFlight() != null && u.getFlight().getFlightDate() != null
                    ? u.getFlight().getFlightDate().toString() : null);
            row.put("position", u.getPosition());
            row.put("status", u.getStatus() != null ? u.getStatus().name() : null);
            row.put("tareLbs", u.getTareLbs());
            row.put("grossWeightLbs", u.getGrossWeightLbs());
            row.put("netWeightLbs", u.getNetWeightLbs());
            row.put("tareKg", u.getTareKg());
            row.put("grossWeightKg", u.getGrossWeightKg());
            row.put("netWeightKg", u.getNetWeightKg());
            row.put("sealNumber", u.getSealNumber());

            List<com.aircargo.entity.UldAwb> awbs = uldAwbRepository.findByUldId(u.getId());
            row.put("awbCount", awbs.size());
            row.put("totalPieces", awbs.stream()
                    .mapToInt(a -> a.getPieces() != null ? a.getPieces() : 0)
                    .sum());
            row.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
            return row;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getDashboard() {
        Map<String, Object> kpis = new LinkedHashMap<>();

        List<com.aircargo.entity.Flight> flights = flightRepository.findAll();
        List<com.aircargo.entity.Booking> bookings = bookingRepository.findAll();
        List<com.aircargo.entity.Mawb> mawbs = mawbRepository.findAll();
        List<com.aircargo.entity.Uld> ulds = uldRepository.findAll();
        List<com.aircargo.entity.WarehouseReceipt> receipts = receiptRepository.findAll();

        kpis.put("totalFlights", flights.size());
        kpis.put("flightsScheduled", flights.stream()
                .filter(f -> f.getStatus() == com.aircargo.entity.FlightStatus.SCHEDULED).count());
        kpis.put("flightsBoarding", flights.stream()
                .filter(f -> f.getStatus() == com.aircargo.entity.FlightStatus.BOARDING).count());
        kpis.put("flightsDeparted", flights.stream()
                .filter(f -> f.getStatus() == com.aircargo.entity.FlightStatus.DEPARTED).count());
        kpis.put("flightsArrived", flights.stream()
                .filter(f -> f.getStatus() == com.aircargo.entity.FlightStatus.ARRIVED).count());

        kpis.put("totalBookings", bookings.size());
        kpis.put("totalReservedKg", bookings.stream()
                .map(b -> b.getReservedKg() != null ? b.getReservedKg() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        kpis.put("totalReceivedKg", bookings.stream()
                .map(b -> b.getReceivedKg() != null ? b.getReceivedKg() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        kpis.put("totalMawbs", mawbs.size());
        kpis.put("mawbsBooked", mawbs.stream()
                .filter(m -> m.getStatus() == com.aircargo.entity.MawbStatus.BOOKED).count());
        kpis.put("mawbsReceived", mawbs.stream()
                .filter(m -> m.getStatus() == com.aircargo.entity.MawbStatus.RECEIVED).count());
        kpis.put("mawbsManifested", mawbs.stream()
                .filter(m -> m.getStatus() == com.aircargo.entity.MawbStatus.MANIFESTED).count());
        kpis.put("mawbsDeparted", mawbs.stream()
                .filter(m -> m.getStatus() == com.aircargo.entity.MawbStatus.DEPARTED).count());

        kpis.put("totalPieces", mawbs.stream()
                .mapToInt(m -> m.getPieces() != null ? m.getPieces() : 0)
                .sum());
        kpis.put("totalReportedWeightKg", mawbs.stream()
                .map(m -> m.getReportedWeightKg() != null ? m.getReportedWeightKg() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        kpis.put("totalUlds", ulds.size());
        kpis.put("uldsOpen", ulds.stream()
                .filter(u -> u.getStatus() == com.aircargo.entity.UldStatus.OPEN).count());
        kpis.put("uldsBuilt", ulds.stream()
                .filter(u -> u.getStatus() == com.aircargo.entity.UldStatus.BUILT).count());
        kpis.put("uldsSealed", ulds.stream()
                .filter(u -> u.getStatus() == com.aircargo.entity.UldStatus.SEALED).count());
        kpis.put("uldsLoaded", ulds.stream()
                .filter(u -> u.getStatus() == com.aircargo.entity.UldStatus.LOADED).count());

        kpis.put("totalReceipts", receipts.size());

        BigDecimal avgFulfillment = bookings.stream()
                .filter(b -> b.getFulfillmentPct() != null)
                .map(com.aircargo.entity.Booking::getFulfillmentPct)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long bookingsWithFulfillment = bookings.stream()
                .filter(b -> b.getFulfillmentPct() != null).count();
        if (bookingsWithFulfillment > 0) {
            kpis.put("avgFulfillmentPct", avgFulfillment
                    .divide(BigDecimal.valueOf(bookingsWithFulfillment), 4, RoundingMode.HALF_UP));
        } else {
            kpis.put("avgFulfillmentPct", BigDecimal.ZERO);
        }

        return kpis;
    }

    public List<Map<String, Object>> getDaily(LocalDate dateFrom, LocalDate dateTo) {
        List<com.aircargo.entity.Flight> flights = flightRepository.findAll();
        List<com.aircargo.entity.Mawb> mawbs = mawbRepository.findAll();
        List<com.aircargo.entity.WarehouseReceipt> receipts = receiptRepository.findAll();

        Set<LocalDate> allDates = new TreeSet<>();
        flights.stream().map(com.aircargo.entity.Flight::getFlightDate).filter(Objects::nonNull).forEach(allDates::add);
        mawbs.stream().filter(m -> m.getCreatedAt() != null)
                .map(m -> m.getCreatedAt().toLocalDate()).forEach(allDates::add);
        receipts.stream().filter(r -> r.getCreatedAt() != null)
                .map(r -> r.getCreatedAt().toLocalDate()).forEach(allDates::add);

        if (dateFrom != null) allDates = allDates.stream().filter(d -> !d.isBefore(dateFrom)).collect(Collectors.toCollection(TreeSet::new));
        if (dateTo != null) allDates = allDates.stream().filter(d -> !d.isAfter(dateTo)).collect(Collectors.toCollection(TreeSet::new));

        return allDates.stream().map(date -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("date", date.toString());

            long flightsCount = flights.stream()
                    .filter(f -> date.equals(f.getFlightDate())).count();
            row.put("flightsCount", flightsCount);

            long mawbsCount = mawbs.stream()
                    .filter(m -> m.getCreatedAt() != null && date.equals(m.getCreatedAt().toLocalDate()))
                    .count();
            row.put("mawbsCount", mawbsCount);

            int piecesTotal = mawbs.stream()
                    .filter(m -> m.getCreatedAt() != null && date.equals(m.getCreatedAt().toLocalDate()))
                    .mapToInt(m -> m.getPieces() != null ? m.getPieces() : 0)
                    .sum();
            row.put("piecesTotal", piecesTotal);

            BigDecimal weightTotal = mawbs.stream()
                    .filter(m -> m.getCreatedAt() != null && date.equals(m.getCreatedAt().toLocalDate()))
                    .map(m -> m.getReportedWeightKg() != null ? m.getReportedWeightKg() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            row.put("weightTotalKg", weightTotal);

            long receiptsCount = receipts.stream()
                    .filter(r -> r.getCreatedAt() != null && date.equals(r.getCreatedAt().toLocalDate()))
                    .count();
            row.put("receiptsCount", receiptsCount);

            return row;
        }).collect(Collectors.toList());
    }
}
