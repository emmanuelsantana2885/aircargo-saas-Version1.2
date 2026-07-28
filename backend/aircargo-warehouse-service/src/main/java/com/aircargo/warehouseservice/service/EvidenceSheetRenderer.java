package com.aircargo.warehouseservice.service;

import com.aircargo.warehouseservice.entity.WarehouseReceipt;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;
import java.util.Map;

final class EvidenceSheetRenderer {

    private static final Logger log = LoggerFactory.getLogger(EvidenceSheetRenderer.class);

    private final ObjectMapper objectMapper;

    EvidenceSheetRenderer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void create(XSSFWorkbook xssfWorkbook, WarehouseReceipt receipt, ExcelExportStyles styles) throws Exception {
        Sheet evSheet = xssfWorkbook.createSheet("Evidencias");

        evSheet.setColumnWidth(0, (int) (3.57 * 256));
        evSheet.setColumnWidth(1, (int) (40 * 256));
        evSheet.setColumnWidth(2, (int) (15 * 256));
        evSheet.setColumnWidth(3, (int) (60 * 256));

        int r = 0;

        // Title
        Row titleRow = evSheet.createRow(r++);
        titleRow.setHeightInPoints(24);
        Cell t = titleRow.createCell(0);
        t.setCellValue("EVIDENCIAS DOCUMENTALES \u2014 RECIBO DE BODEGA");
        t.setCellStyle(styles.evTitleStyle);
        for (int c = 1; c <= 3; c++) titleRow.createCell(c).setCellStyle(styles.evTitleStyle);
        evSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

        // Info row
        Row infoRow = evSheet.createRow(r++);
        infoRow.setHeightInPoints(18);
        Cell info = infoRow.createCell(0);
        String mawbNum = receipt.getMawbNumber() != null ? receipt.getMawbNumber() : "\u2014";
        String shipper = ExcelExportStyles.safeStr(receipt.getShipperName(), "\u2014");
        info.setCellValue("MAWB: " + mawbNum + " | Shipper: " + shipper + " | Fecha: " +
                (receipt.getReceiptDate() != null ? receipt.getReceiptDate().toString().substring(0, 10) : "\u2014"));
        info.setCellStyle(styles.evLabelStyle);
        for (int c = 1; c <= 3; c++) infoRow.createCell(c).setCellStyle(styles.evLabelStyle);
        evSheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));
        r++;

        List<Map<String, String>> docs = parseSupportingDocs(receipt);

        if (docs.isEmpty()) {
            Row emptyRow = evSheet.createRow(r++);
            emptyRow.createCell(0).setCellValue("No hay evidencias registradas para este recibo.");
            emptyRow.getCell(0).setCellStyle(styles.evLabelStyle);
            evSheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 3));
            evSheet.protectSheet("aircargo2024");
            return;
        }

        renderSignatures(evSheet, receipt, styles, xssfWorkbook, r);
        r = evSheet.getLastRowNum() + 1;

        renderDocuments(evSheet, xssfWorkbook, docs, styles, r);

        evSheet.protectSheet("aircargo2024");
    }

    private void renderSignatures(Sheet evSheet, WarehouseReceipt receipt, ExcelExportStyles styles, XSSFWorkbook xssfWb, int startRow) {
        int r = startRow;

        Row sigHeader = evSheet.createRow(r++);
        sigHeader.setHeightInPoints(18);
        Cell sigH = sigHeader.createCell(0);
        sigH.setCellValue("FIRMAS CAPTURADAS");
        sigH.setCellStyle(styles.evTitleStyle);
        for (int c = 1; c <= 3; c++) sigHeader.createCell(c).setCellStyle(styles.evTitleStyle);
        evSheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 3));

        if (receipt.getDockSignature() != null && !receipt.getDockSignature().isEmpty()) {
            r = renderSigRow(evSheet, xssfWb, r, "Dock Signature:", styles,
                    "Dock Signature \u2014 " + ExcelExportStyles.safeStr(receipt.getPrintName(), ""),
                    receipt.getDockSignature());
        }

        if (receipt.getDeliveredBySigUrl() != null && !receipt.getDeliveredBySigUrl().isEmpty()) {
            String delLabel = "Delivered By: " + ExcelExportStyles.safeStr(receipt.getDeliveredByName(), "") +
                    (receipt.getDeliveredByIdNum() != null ? " (ID: " + receipt.getDeliveredByIdNum() + ")" : "");
            r = renderSigRow(evSheet, xssfWb, r, delLabel, styles,
                    "Delivered By \u2014 " + ExcelExportStyles.safeStr(receipt.getDeliveredByName(), ""),
                    receipt.getDeliveredBySigUrl());
        }

        if (receipt.getBrokerSigUrl() != null && !receipt.getBrokerSigUrl().isEmpty()) {
            String brokerLabel = "Broker Rep: " + ExcelExportStyles.safeStr(receipt.getBrokerName(), "") +
                    (receipt.getBrokerIdNum() != null ? " (ID: " + receipt.getBrokerIdNum() + ")" : "");
            r = renderSigRow(evSheet, xssfWb, r, brokerLabel, styles,
                    "Broker \u2014 " + ExcelExportStyles.safeStr(receipt.getBrokerName(), ""),
                    receipt.getBrokerSigUrl());
        }
    }

    private int renderSigRow(Sheet evSheet, XSSFWorkbook xssfWb, int r, String label,
                             ExcelExportStyles styles, String imgLabel, String sigUrl) {
        Row row = evSheet.createRow(r++);
        row.setHeightInPoints(20);
        Cell l = row.createCell(0);
        l.setCellValue(label);
        l.setCellStyle(styles.evHeaderStyle);
        for (int c = 1; c <= 3; c++) row.createCell(c).setCellStyle(styles.evHeaderStyle);
        evSheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 3));

        embedBase64Image(xssfWb, evSheet, sigUrl, r++, imgLabel);
        return r;
    }

    private void renderDocuments(Sheet evSheet, XSSFWorkbook xssfWb, List<Map<String, String>> docs,
                                 ExcelExportStyles styles, int startRow) {
        int r = startRow;

        Row docsHeader = evSheet.createRow(r++);
        docsHeader.setHeightInPoints(18);
        Cell dh = docsHeader.createCell(0);
        dh.setCellValue("DOCUMENTOS DE SOPORTE");
        dh.setCellStyle(styles.evTitleStyle);
        for (int c = 1; c <= 3; c++) docsHeader.createCell(c).setCellStyle(styles.evTitleStyle);
        evSheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 3));

        Row tblHeader = evSheet.createRow(r++);
        tblHeader.setHeightInPoints(20);
        String[] cols = {"#", "Nombre", "Tipo", "Vista Previa"};
        for (int c = 0; c < cols.length; c++) {
            Cell cell = tblHeader.createCell(c);
            cell.setCellValue(cols[c]);
            cell.setCellStyle(styles.evHeaderStyle);
        }

        for (int i = 0; i < docs.size(); i++) {
            Map<String, String> doc = docs.get(i);
            String name = doc.getOrDefault("name", "Documento " + (i + 1));
            String type = doc.getOrDefault("type", "document");
            String url = doc.getOrDefault("url", "");

            Row row = evSheet.createRow(r++);
            row.setHeightInPoints(25);
            Cell numCell = row.createCell(0);
            numCell.setCellValue(i + 1);
            numCell.setCellStyle(styles.evDataStyle);
            Cell nameCell = row.createCell(1);
            nameCell.setCellValue(name);
            nameCell.setCellStyle(styles.evDataStyle);
            Cell typeCell = row.createCell(2);
            typeCell.setCellValue("image".equals(type) ? "Imagen" : "Documento");
            typeCell.setCellStyle(styles.evDataStyle);

            if ("image".equals(type) && url != null && !url.isEmpty()) {
                embedBase64Image(xssfWb, evSheet, url, r - 1, name);
            } else {
                Cell cell = row.createCell(3);
                cell.setCellValue("Documento (" + name + ")");
                cell.setCellStyle(styles.evDataStyle);
            }
        }
    }

    private void embedBase64Image(XSSFWorkbook xssfWb, Sheet sheet, String url, int rowNum, String label) {
        if (url == null || url.isEmpty()) return;
        try {
            String base64Data = url.contains(",") ? url.substring(url.indexOf(",") + 1) : url;
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            int pictureType = Workbook.PICTURE_TYPE_PNG;
            if (imageBytes.length > 4) {
                if (imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8) {
                    pictureType = Workbook.PICTURE_TYPE_JPEG;
                }
            }
            int pictureIdx = xssfWb.addPicture(imageBytes, pictureType);
            var drawing = sheet.createDrawingPatriarch();
            var anchor = new XSSFClientAnchor(0, 0, 0, 0, 1, rowNum, 4, rowNum + 3);
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
            var picture = drawing.createPicture(anchor, pictureIdx);
            picture.resize(1.0);
        } catch (Exception e) {
            log.warn("Could not embed image '{}' at row {}: {}", label, rowNum, e.getMessage());
            Row row = sheet.getRow(rowNum);
            if (row != null) {
                Cell cell = row.createCell(3);
                cell.setCellValue("[" + label + "]");
                Workbook wb = sheet.getWorkbook();
                CellStyle fallback = wb.createCellStyle();
                Font f = wb.createFont();
                f.setFontHeightInPoints((short) 10);
                f.setFontName("Tahoma");
                fallback.setFont(f);
                fallback.setAlignment(HorizontalAlignment.CENTER);
                fallback.setVerticalAlignment(VerticalAlignment.CENTER);
                fallback.setBorderTop(BorderStyle.THIN);
                fallback.setBorderBottom(BorderStyle.THIN);
                fallback.setBorderLeft(BorderStyle.THIN);
                fallback.setBorderRight(BorderStyle.THIN);
                fallback.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                fallback.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cell.setCellStyle(fallback);
            }
        }
    }

    private List<Map<String, String>> parseSupportingDocs(WarehouseReceipt receipt) {
        try {
            String raw = receipt.getSupportingDocs();
            if (raw != null && !raw.isEmpty() && !"[]".equals(raw)) {
                return objectMapper.readValue(raw, new TypeReference<List<Map<String, String>>>() {});
            }
        } catch (Exception e) {
            log.warn("Could not parse supporting docs for receipt {}: {}", receipt.getId(), e.getMessage());
        }
        return List.of();
    }
}
