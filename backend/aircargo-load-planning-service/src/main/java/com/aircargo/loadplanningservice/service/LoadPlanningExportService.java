package com.aircargo.loadplanningservice.service;

import com.aircargo.feign.client.UldClient;
import com.aircargo.feign.dto.UldDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.UUID;

@Service
public class LoadPlanningExportService {

    private final UldClient uldClient;

    public LoadPlanningExportService(UldClient uldClient) {
        this.uldClient = uldClient;
    }

    public ByteArrayInputStream exportFlightLoadPlan(UUID flightId) throws Exception {
        List<UldDTO> ulds = uldClient.getUlds(null, flightId);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("MANIFIESTO_ESTIBA");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 10);

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setBorderTop(BorderStyle.THIN);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ULD NUMBER", "TYPE", "POSITION", "CONFIG", "SEAL #", "TARE (LBS)", "GROSS WT (LBS)", "STATUS"};

            for (int col = 0; col < columns.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIndex = 1;
            for (UldDTO uld : ulds) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(uld.getUldNumber() != null ? uld.getUldNumber() : "");
                row.getCell(0).setCellStyle(dataCellStyle);

                row.createCell(1).setCellValue(uld.getUldType() != null ? uld.getUldType() : "");
                row.getCell(1).setCellStyle(dataCellStyle);

                row.createCell(2).setCellValue(uld.getPosition() != null ? uld.getPosition() : "W/O");
                row.getCell(2).setCellStyle(dataCellStyle);

                row.createCell(3).setCellValue(uld.getConfig() != null ? uld.getConfig() : "");
                row.getCell(3).setCellStyle(dataCellStyle);

                row.createCell(4).setCellValue(uld.getSealNumber() != null ? uld.getSealNumber() : "-");
                row.getCell(4).setCellStyle(dataCellStyle);

                row.createCell(5).setCellValue(uld.getTareLbs() != null ? uld.getTareLbs().doubleValue() : 0.0);
                row.getCell(5).setCellStyle(dataCellStyle);

                row.createCell(6).setCellValue(uld.getGrossWeightLbs() != null ? uld.getGrossWeightLbs().doubleValue() : 0.0);
                row.getCell(6).setCellStyle(dataCellStyle);

                row.createCell(7).setCellValue(uld.getStatus() != null ? uld.getStatus() : "OPEN");
                row.getCell(7).setCellStyle(dataCellStyle);
            }

            for (int col = 0; col < columns.length; col++) {
                sheet.autoSizeColumn(col);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
