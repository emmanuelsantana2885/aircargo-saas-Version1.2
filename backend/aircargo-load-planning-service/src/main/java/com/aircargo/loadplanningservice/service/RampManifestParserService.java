package com.aircargo.loadplanningservice.service;

import com.aircargo.feign.client.FlightClient;
import com.aircargo.feign.client.UldClient;
import com.aircargo.feign.dto.FlightDTO;
import com.aircargo.feign.dto.UldDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class RampManifestParserService {

    private final FlightClient flightClient;
    private final UldClient uldClient;

    public RampManifestParserService(FlightClient flightClient, UldClient uldClient) {
        this.flightClient = flightClient;
        this.uldClient = uldClient;
    }

    public List<UldDTO> parseExcelToNativeUld(MultipartFile file, UUID flightId, UUID airlineId) throws Exception {
        Map<String, UldDTO> uldMap = new LinkedHashMap<>();
        FlightDTO flight = flightClient.getFlightById(flightId);

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 2; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String uldNumberStr = getCellValueResolved(sheet, row.getCell(0));
                String mawb = getCellValueResolved(sheet, row.getCell(9));

                if ((uldNumberStr == null || uldNumberStr.isEmpty()) && (mawb == null || mawb.isEmpty())) {
                    continue;
                }

                if (uldNumberStr != null && !uldNumberStr.isEmpty()) {
                    uldNumberStr = uldNumberStr.trim();
                    if (!uldMap.containsKey(uldNumberStr)) {
                        String weightStr = getCellValueResolved(sheet, row.getCell(3));
                        String taraStr = getCellValueResolved(sheet, row.getCell(4));
                        String configStr = getCellValueResolved(sheet, row.getCell(5));
                        String posStr = getCellValueResolved(sheet, row.getCell(7));

                        double grossAmount = (weightStr != null && !weightStr.isEmpty()) ? Double.parseDouble(weightStr) : 0.0;
                        double tareAmount = (taraStr != null && !taraStr.isEmpty()) ? Double.parseDouble(taraStr) : 0.0;

                        UldDTO uld = new UldDTO();
                        uld.setUldNumber(uldNumberStr);
                        uld.setFlightId(flightId);
                        uld.setAirlineId(airlineId);
                        uld.setPosition(posStr != null ? posStr.trim() : "");
                        uld.setConfig(configStr != null ? configStr.trim() : "AAZ");
                        uld.setGrossWeightLbs(BigDecimal.valueOf(grossAmount));
                        uld.setTareLbs(BigDecimal.valueOf(tareAmount));

                        String typeClean = configStr != null ? configStr.trim().toUpperCase() : "PMC";
                        uld.setUldType(typeClean);
                        uld.setStatus("OPEN");

                        uldMap.put(uldNumberStr, uld);
                    }
                }
            }
        }

        return new ArrayList<>(uldMap.values());
    }

    private String getCellValueResolved(Sheet sheet, Cell cell) {
        if (cell == null) return "";

        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            if (region.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
                Row masterRow = sheet.getRow(region.getFirstRow());
                Cell masterCell = masterRow.getCell(region.getFirstColumn());
                return getFormatterCellValue(masterCell);
            }
        }
        return getFormatterCellValue(cell);
    }

    private String getFormatterCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }
}
