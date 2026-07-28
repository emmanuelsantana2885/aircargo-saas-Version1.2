package com.aircargo.warehouseservice.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

final class ExcelExportStyles {

    private final Font fontTahoma8;
    private final Font fontTahoma10;
    private final Font fontTahoma10Bold;
    private final Font fontTahoma14Bold;
    private final Font fontTahoma10Red;
    private final Font fontTahoma10BoldRed;
    private final Font fontTahoma8Red;

    final CellStyle s1RowNum;
    final CellStyle s2Title;
    final CellStyle s4Plain;
    final CellStyle s5LabelRight;
    final CellStyle s6SpacerCenter;
    final CellStyle s7Section;
    final CellStyle s8SectionCenter;
    final CellStyle s9GreyFill;
    final CellStyle s10SectionHeader;
    final CellStyle s11Th;
    final CellStyle s12WeightLabel;
    final CellStyle s13Data;
    final CellStyle s14NHelper;
    final CellStyle s15DimWeight;
    final CellStyle s16TotalDim;
    final CellStyle s17TotalBold;
    final CellStyle s20RedPlain;
    final CellStyle s22Value;
    final CellStyle s25DimLabel;
    final CellStyle s26BoldEmpty;
    final CellStyle s29GHeader;
    final CellStyle s33RedData;
    final CellStyle s37TotalRed;
    final CellStyle s38WeightVal;
    final CellStyle s42WeightStr;
    final CellStyle s43LabelMBottom;
    final CellStyle s44HeaderLabel;
    final CellStyle s45HeaderVal;
    final CellStyle s46WeightVal2;
    final CellStyle s47HeaderVal2;
    final CellStyle s48TitleBg;
    final CellStyle s50PieceVal;
    final CellStyle s51Remarks;
    final CellStyle s58DateVal;

    // Evidence sheet styles
    final CellStyle evTitleStyle;
    final CellStyle evHeaderStyle;
    final CellStyle evDataStyle;
    final CellStyle evLabelStyle;
    final CellStyle evImgLabelStyle;

    // Evidence sheet fonts
    final Font evTahoma10;
    final Font evTahoma11Bold;
    final Font evTahoma12Bold;

    ExcelExportStyles(Workbook wb) {
        this.fontTahoma8 = createFont(wb, (short) 8, "Tahoma", false, null);
        this.fontTahoma10 = createFont(wb, (short) 10, "Tahoma", false, null);
        this.fontTahoma10Bold = createFont(wb, (short) 10, "Tahoma", true, null);
        this.fontTahoma14Bold = createFont(wb, (short) 14, "Tahoma", true, null);
        this.fontTahoma10Red = createFont(wb, (short) 10, "Tahoma", false, IndexedColors.RED);
        this.fontTahoma10BoldRed = createFont(wb, (short) 10, "Tahoma", true, IndexedColors.RED);
        this.fontTahoma8Red = createFont(wb, (short) 8, "Tahoma", false, IndexedColors.RED);

        this.evTahoma10 = createFont(wb, (short) 10, "Tahoma", false, null);
        this.evTahoma11Bold = createFont(wb, (short) 11, "Tahoma", true, null);
        this.evTahoma12Bold = createFont(wb, (short) 12, "Tahoma", true, null);

        this.s1RowNum = build(wb, fontTahoma8, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, false, true);
        this.s2Title = build(wb, fontTahoma14Bold, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.s4Plain = build(wb, fontTahoma10, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.s5LabelRight = build(wb, fontTahoma10, HorizontalAlignment.RIGHT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.s6SpacerCenter = build(wb, fontTahoma10, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.s6SpacerCenter.setWrapText(true);
        this.s7Section = build(wb, fontTahoma10, HorizontalAlignment.RIGHT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, IndexedColors.GREY_25_PERCENT, true, true);
        this.s8SectionCenter = build(wb, fontTahoma10, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, IndexedColors.GREY_25_PERCENT, true, true);
        this.s8SectionCenter.setWrapText(true);
        this.s9GreyFill = build(wb, fontTahoma10, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, IndexedColors.GREY_25_PERCENT, true, true);
        this.s10SectionHeader = build(wb, fontTahoma10Bold, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.s11Th = build(wb, fontTahoma10, HorizontalAlignment.CENTER, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT, true, true);
        this.s12WeightLabel = build(wb, fontTahoma10Bold, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, IndexedColors.LIGHT_YELLOW, true, true);
        this.s13Data = build(wb, fontTahoma10, HorizontalAlignment.GENERAL, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null, false, true);
        this.s13Data.setDataFormat(wb.createDataFormat().getFormat("0"));
        this.s14NHelper = build(wb, fontTahoma10Red, HorizontalAlignment.GENERAL, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null, false, true);
        this.s14NHelper.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
        this.s15DimWeight = build(wb, fontTahoma10, HorizontalAlignment.GENERAL, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null, false, true);
        this.s15DimWeight.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        this.s16TotalDim = build(wb, fontTahoma10Bold, HorizontalAlignment.GENERAL, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null, false, true);
        this.s16TotalDim.setDataFormat(wb.createDataFormat().getFormat("0.00"));
        this.s17TotalBold = build(wb, fontTahoma10Bold, HorizontalAlignment.GENERAL, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null, false, true);
        this.s17TotalBold.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
        this.s20RedPlain = build(wb, fontTahoma10Red, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.s22Value = build(wb, fontTahoma10, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.MEDIUM, null, false, true);
        this.s25DimLabel = build(wb, fontTahoma8Red, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.s26BoldEmpty = build(wb, fontTahoma10Bold, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.s29GHeader = build(wb, fontTahoma10Red, HorizontalAlignment.CENTER, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT, true, true);
        this.s29GHeader.setWrapText(true);
        this.s33RedData = build(wb, fontTahoma10Red, HorizontalAlignment.GENERAL, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null, true, true);
        this.s33RedData.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
        this.s37TotalRed = build(wb, fontTahoma10BoldRed, HorizontalAlignment.GENERAL, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null, true, true);
        this.s37TotalRed.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
        this.s38WeightVal = build(wb, fontTahoma10Bold, HorizontalAlignment.RIGHT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, IndexedColors.LIGHT_YELLOW, false, true);
        this.s42WeightStr = build(wb, fontTahoma10Bold, HorizontalAlignment.RIGHT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, IndexedColors.LIGHT_YELLOW, false, true);
        this.s43LabelMBottom = build(wb, fontTahoma10, HorizontalAlignment.RIGHT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.MEDIUM, null, true, true);
        this.s44HeaderLabel = build(wb, fontTahoma10, HorizontalAlignment.RIGHT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.s45HeaderVal = build(wb, fontTahoma10, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.MEDIUM, null, false, true);
        this.s46WeightVal2 = build(wb, fontTahoma10, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.MEDIUM, null, false, true);
        this.s46WeightVal2.setDataFormat(wb.createDataFormat().getFormat("#,##0"));
        this.s47HeaderVal2 = build(wb, fontTahoma10, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.MEDIUM, null, false, true);
        this.s48TitleBg = build(wb, fontTahoma14Bold, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.s50PieceVal = build(wb, fontTahoma10, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.MEDIUM, null, false, true);
        this.s50PieceVal.setDataFormat(wb.createDataFormat().getFormat("0"));
        this.s51Remarks = build(wb, fontTahoma10, HorizontalAlignment.GENERAL, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null, false, true);
        this.s58DateVal = build(wb, fontTahoma10, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.MEDIUM, null, false, true);
        this.s58DateVal.setDataFormat(wb.createDataFormat().getFormat("mm-dd-yy"));

        this.evTitleStyle = build(wb, evTahoma12Bold, HorizontalAlignment.CENTER, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.evHeaderStyle = build(wb, evTahoma11Bold, HorizontalAlignment.CENTER, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, IndexedColors.GREY_25_PERCENT, true, true);
        this.evDataStyle = build(wb, evTahoma10, HorizontalAlignment.CENTER, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, null, true, true);
        this.evLabelStyle = build(wb, evTahoma10, HorizontalAlignment.LEFT, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, null, true, true);
        this.evImgLabelStyle = build(wb, evTahoma10, HorizontalAlignment.CENTER, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, IndexedColors.LIGHT_YELLOW, true, true);
    }

    private static Font createFont(Workbook wb, short size, String name, boolean bold, IndexedColors color) {
        Font f = wb.createFont();
        f.setFontHeightInPoints(size);
        f.setFontName(name);
        if (bold) f.setBold(true);
        if (color != null) f.setColor(color.getIndex());
        return f;
    }

    private static CellStyle build(Workbook wb, Font font, HorizontalAlignment align,
                                   BorderStyle left, BorderStyle right, BorderStyle top, BorderStyle bottom,
                                   IndexedColors fill, boolean locked, boolean wrap) {
        CellStyle s = wb.createCellStyle();
        s.setFont(font);
        s.setAlignment(align);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        if (left != BorderStyle.NONE) s.setBorderLeft(left);
        if (right != BorderStyle.NONE) s.setBorderRight(right);
        if (top != BorderStyle.NONE) s.setBorderTop(top);
        if (bottom != BorderStyle.NONE) s.setBorderBottom(bottom);
        if (fill != null) {
            s.setFillForegroundColor(fill.getIndex());
            s.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        s.setLocked(locked);
        s.setWrapText(wrap);
        return s;
    }

    static void setCell(Row row, int col, double val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val);
        cell.setCellStyle(style);
    }

    static void setCell(Row row, int col, String val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val);
        cell.setCellStyle(style);
    }

    static void mergeLabel(Sheet sheet, Row row, int startCol, int endCol, String value, CellStyle style) {
        Cell cell = row.createCell(startCol);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        if (startCol != endCol) {
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), startCol, endCol));
            for (int c = startCol + 1; c <= endCol; c++) {
                row.createCell(c).setCellStyle(style);
            }
        }
    }

    static void blankRow(Sheet sheet, int rowNum, float height) {
        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(height);
    }

    static void blankStyledRow(Sheet sheet, int rowNum, CellStyle aStyle, CellStyle bStyle) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellStyle(aStyle);
        row.createCell(1).setCellStyle(bStyle);
        for (int c = 7; c <= 12; c++) row.createCell(c).setCellStyle(bStyle);
    }

    static String safeStr(String s, String fallback) {
        return s != null && !s.isEmpty() ? s : fallback;
    }

    static long round0(double v) {
        return Math.round(v);
    }

    static String chkStr(boolean val, String label) {
        return label;
    }

    static void buildHeaderRow(Sheet sheet, int rowNum, String label, String value, String checkboxText,
                               CellStyle labelStyle, CellStyle valStyle, CellStyle valStyle2) {
        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(13.5f);
        mergeLabel(sheet, row, 1, 2, label, labelStyle);
        Cell vc = row.createCell(3);
        vc.setCellValue(value);
        vc.setCellStyle(valStyle);
        row.createCell(4).setCellStyle(valStyle2);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 3, 4));
        if (checkboxText != null) {
            mergeLabel(sheet, row, 5, 6, checkboxText, labelStyle);
        } else {
            for (int c = 5; c <= 6; c++) row.createCell(c).setCellStyle(labelStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 5, 6));
        }
    }
}
