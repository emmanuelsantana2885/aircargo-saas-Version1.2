package com.aircargo.service;

import com.aircargo.dto.LoadPlanningBatchImportResultDTO;
import com.aircargo.dto.LoadPlanningSheetImportResultDTO;
import com.aircargo.entity.*;
import com.aircargo.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Locale;
import com.aircargo.common.entity.Airline;
import com.aircargo.common.entity.CommodityType;
import java.util.regex.Pattern;

@Service
public class LoadPlanningBatchImportService {

    private static final Pattern AWB_PATTERN = Pattern.compile("^\\d{3}-\\d{8}$");

    private final FlightRepository flightRepository;
    private final UldRepository uldRepository;
    private final UldAwbRepository uldAwbRepository;
    private final MawbRepository mawbRepository;
    private final BookingRepository bookingRepository;
    private final AirlineRepository airlineRepository;

    public LoadPlanningBatchImportService(FlightRepository flightRepository,
                                           UldRepository uldRepository,
                                           UldAwbRepository uldAwbRepository,
                                           MawbRepository mawbRepository,
                                           BookingRepository bookingRepository,
                                           AirlineRepository airlineRepository) {
        this.flightRepository = flightRepository;
        this.uldRepository = uldRepository;
        this.uldAwbRepository = uldAwbRepository;
        this.mawbRepository = mawbRepository;
        this.bookingRepository = bookingRepository;
        this.airlineRepository = airlineRepository;
    }

    @Transactional
    public LoadPlanningBatchImportResultDTO importBatch(MultipartFile file) throws IOException {
        List<LoadPlanningSheetImportResultDTO> sheetResults = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            int totalSheets = workbook.getNumberOfSheets();

            for (int s = 0; s < totalSheets; s++) {
                Sheet sheet = workbook.getSheetAt(s);
                String sheetName = sheet.getSheetName();
                sheetResults.add(importSheet(sheet, sheetName));
            }
        }

        int successSheets = (int) sheetResults.stream().filter(LoadPlanningSheetImportResultDTO::isSuccess).count();
        int failedSheets = sheetResults.size() - successSheets;
        int totalUldsCreated = sheetResults.stream().mapToInt(LoadPlanningSheetImportResultDTO::getUldsCreated).sum();
        int totalUldsUpdated = sheetResults.stream().mapToInt(LoadPlanningSheetImportResultDTO::getUldsUpdated).sum();
        int totalMawbsCreated = sheetResults.stream().mapToInt(LoadPlanningSheetImportResultDTO::getMawbsCreated).sum();
        int totalBookingsCreated = sheetResults.stream().mapToInt(LoadPlanningSheetImportResultDTO::getBookingsCreated).sum();
        int totalUldAwbsCreated = sheetResults.stream().mapToInt(LoadPlanningSheetImportResultDTO::getUldAwbsCreated).sum();

        return LoadPlanningBatchImportResultDTO.builder()
                .totalSheets(sheetResults.size())
                .successSheets(successSheets)
                .failedSheets(failedSheets)
                .totalUldsCreated(totalUldsCreated)
                .totalUldsUpdated(totalUldsUpdated)
                .totalMawbsCreated(totalMawbsCreated)
                .totalBookingsCreated(totalBookingsCreated)
                .totalUldAwbsCreated(totalUldAwbsCreated)
                .sheetResults(sheetResults)
                .build();
    }

    private LoadPlanningSheetImportResultDTO importSheet(Sheet sheet, String sheetName) {
        List<String> warnings = new ArrayList<>();
        int uldsCreated = 0, uldsUpdated = 0, mawbsCreated = 0, bookingsCreated = 0, uldAwbsCreated = 0;

        try {
            Row headerRow = sheet.getRow(2);
            if (headerRow == null) {
                return fail(sheetName, "Fila de encabezado (fila 3) no encontrada en la hoja '" + sheetName + "'");
            }

            String flightNumber = getStringValue(headerRow.getCell(4));
            LocalDate flightDate = getDateValue(headerRow.getCell(11));

            if (flightNumber == null || flightNumber.isBlank()) {
                return fail(sheetName, "No se pudo leer el numero de vuelo en E3 de la hoja '" + sheetName + "'");
            }
            if (flightDate == null) {
                return fail(sheetName, "No se pudo leer la fecha del vuelo en L3 de la hoja '" + sheetName + "'");
            }

            Flight flight = flightRepository.findAll().stream()
                    .filter(f -> flightNumber.trim().equalsIgnoreCase(f.getFlightNumber().trim())
                            && flightDate.equals(f.getFlightDate()))
                    .findFirst()
                    .orElse(null);

            if (flight == null) {
                return fail(sheetName, "No existe un vuelo " + flightNumber + " con fecha " + flightDate
                        + " (hoja '" + sheetName + "'). Crea el vuelo primero.");
            }

            UUID flightId = flight.getId();
            Airline airline = flight.getAirline();
            Uld currentUld = null;

            for (int rowIdx = 7; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;

                String uldNumber = getStringValue(row.getCell(1));
                String pcsStr = getStringValue(row.getCell(2));
                String pctStr = getStringValue(row.getCell(3));
                BigDecimal grossLbs = getNumericValue(row.getCell(4));
                BigDecimal tareLbs = getNumericValue(row.getCell(5));
                String config = getStringValue(row.getCell(6));
                String seal = getStringValue(row.getCell(7));
                String position = getStringValue(row.getCell(8));
                String description = getStringValue(row.getCell(9));
                String guia = getStringValue(row.getCell(10));
                String dest = getStringValue(row.getCell(11));

                if ((guia == null || guia.isBlank()) && (dest == null || dest.isBlank())) {
                    continue;
                }

                if (uldNumber != null && !uldNumber.isBlank()) {
                    Uld uld = uldRepository.findByFlightIdAndUldNumber(flight.getId(), uldNumber)
                            .orElse(null);

                    boolean isNew = (uld == null);
                    if (isNew) {
                        uld = new Uld();
                        uld.setAirline(airline);
                        uld.setFlight(flight);
                        uld.setUldNumber(uldNumber);
                        uld.setUldType(detectUldType(uldNumber));
                    }

                    if (position != null && !position.isBlank()) uld.setPosition(position);
                    if (config != null && !config.isBlank()) uld.setConfig(config);
                    if (seal != null && !seal.isBlank()) uld.setSealNumber(seal);
                    if (tareLbs != null) uld.setTareLbs(tareLbs);
                    if (grossLbs != null) uld.setGrossWeightLbs(grossLbs);
                    uld.setStatus(UldStatus.LOADED);

                    uld = uldRepository.save(uld);
                    currentUld = uld;

                    if (isNew) uldsCreated++;
                    else uldsUpdated++;
                }

                if (currentUld == null) {
                    warnings.add("Fila " + (rowIdx + 1) + ": guia '" + guia
                            + "' sin ULD asociado. Se omite.");
                    continue;
                }

                Integer pieces = parseIntOrNull(pcsStr);
                Integer piecesPct = parseIntOrNull(pctStr);
                CommodityType commodity = mapCommodityType(description);

                Mawb mawb = null;
                String mawbLabel = null;

                if (guia != null && AWB_PATTERN.matcher(guia.trim()).matches()) {
                    String awbNumber = guia.trim();
                    mawb = mawbRepository.findByAirlineIdAndAwbNumber(airline.getId(), awbNumber)
                            .orElse(null);

                    if (mawb == null) {
                        mawb = new Mawb();
                        mawb.setAirline(airline);
                        mawb.setFlight(flight);
                        mawb.setAwbNumber(awbNumber);
                        mawb.setOrigin(flight.getOrigin());
                        mawb.setDestination(dest != null ? dest : flight.getDestination());
                        mawb.setPieces(pieces != null ? pieces : 1);
                        mawb.setCommodityType(commodity);
                        mawb.setStatus(MawbStatus.ARRIVED);
                        mawb = mawbRepository.save(mawb);
                        mawbsCreated++;

                        warnings.add("MAWB " + awbNumber
                                + " no existia y fue creado desde load planning. "
                                + "Verificar shipper/consignee.");
                    }

                    boolean bookingExists = bookingRepository.findByFlightId(flight.getId()).stream()
                            .anyMatch(b -> awbNumber.equals(b.getAwbNumber()));

                    if (!bookingExists) {
                        Booking booking = new Booking();
                        booking.setAirline(airline);
                        booking.setFlight(flight);
                        booking.setMawb(mawb);
                        booking.setClientName(mawb.getShipperName() != null ? mawb.getShipperName() : "PENDIENTE");
                        booking.setContactName("PENDIENTE");
                        booking.setAwbNumber(awbNumber);
                        booking.setReservedKg(mawb.getReportedWeightKg() != null
                                ? mawb.getReportedWeightKg() : BigDecimal.ZERO);
                        booking.setDestination(dest);
                        booking.setCommodityType(commodity);
                        bookingRepository.save(booking);
                        bookingsCreated++;

                        warnings.add("Booking creado para AWB " + awbNumber
                                + " (no existia reserva para vuelo " + flightNumber + ").");
                    }
                } else {
                    mawbLabel = guia;
                }

                UldAwb uldAwb = new UldAwb();
                uldAwb.setUld(currentUld);
                uldAwb.setMawb(mawb);
                uldAwb.setMawbLabel(mawbLabel);
                uldAwb.setDescription(commodity);
                uldAwb.setDestination(dest);
                uldAwb.setPieces(pieces);
                uldAwb.setPiecesPct(piecesPct);
                uldAwbRepository.save(uldAwb);
                uldAwbsCreated++;
            }

            return LoadPlanningSheetImportResultDTO.builder()
                    .sheetName(sheetName)
                    .flightId(flightId)
                    .flightNumber(flightNumber)
                    .success(true)
                    .uldsCreated(uldsCreated)
                    .uldsUpdated(uldsUpdated)
                    .mawbsCreated(mawbsCreated)
                    .bookingsCreated(bookingsCreated)
                    .uldAwbsCreated(uldAwbsCreated)
                    .warnings(warnings)
                    .build();

        } catch (Exception e) {
            return fail(sheetName, "Error procesando hoja '" + sheetName + "': " + e.getMessage());
        }
    }

    private LoadPlanningSheetImportResultDTO fail(String sheetName, String error) {
        return LoadPlanningSheetImportResultDTO.builder()
                .sheetName(sheetName)
                .success(false)
                .error(error)
                .build();
    }

    // --- Helper methods (same logic as LoadPlanningImportServiceImpl) ---

    private String getStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                String s = cell.getStringCellValue().trim();
                return s.isEmpty() ? null : s;
            case NUMERIC:
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d)) return String.valueOf((long) d);
                return String.valueOf(d);
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    private BigDecimal getNumericValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            }
            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                if (s.isEmpty()) return null;
                return new BigDecimal(s);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private LocalDate getDateValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                LocalDateTime ldt = cell.getLocalDateTimeCellValue();
                return ldt.toLocalDate();
            }
            if (cell.getCellType() == CellType.STRING) {
                String s = cell.getStringCellValue().trim();
                return LocalDate.parse(s);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private Integer parseIntOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return (int) Math.round(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private UldType detectUldType(String uldNumber) {
        if (uldNumber == null) return UldType.BULK;
        String prefix = uldNumber.split("-")[0].toUpperCase(Locale.ROOT);
        try {
            return UldType.valueOf(prefix);
        } catch (IllegalArgumentException e) {
            return UldType.BULK;
        }
    }

    private CommodityType mapCommodityType(String description) {
        if (description == null || description.isBlank()) return CommodityType.DRY_CARGO;
        String normalized = description.trim().toUpperCase(Locale.ROOT).replace(" ", "_");
        try {
            return CommodityType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return CommodityType.GENERAL;
        }
    }
}
