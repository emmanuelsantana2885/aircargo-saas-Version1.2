package com.aircargo.loadplanningservice.dto;

import java.util.List;

public class LoadPlanningBatchImportResultDTO {
    private int totalSheets;
    private int successSheets;
    private int failedSheets;
    private int totalUldsCreated;
    private int totalUldsUpdated;
    private int totalMawbsCreated;
    private int totalBookingsCreated;
    private int totalUldAwbsCreated;
    private List<LoadPlanningSheetImportResultDTO> sheetResults;

    public LoadPlanningBatchImportResultDTO() {}

    public int getTotalSheets() { return totalSheets; }
    public void setTotalSheets(int totalSheets) { this.totalSheets = totalSheets; }
    public int getSuccessSheets() { return successSheets; }
    public void setSuccessSheets(int successSheets) { this.successSheets = successSheets; }
    public int getFailedSheets() { return failedSheets; }
    public void setFailedSheets(int failedSheets) { this.failedSheets = failedSheets; }
    public int getTotalUldsCreated() { return totalUldsCreated; }
    public void setTotalUldsCreated(int totalUldsCreated) { this.totalUldsCreated = totalUldsCreated; }
    public int getTotalUldsUpdated() { return totalUldsUpdated; }
    public void setTotalUldsUpdated(int totalUldsUpdated) { this.totalUldsUpdated = totalUldsUpdated; }
    public int getTotalMawbsCreated() { return totalMawbsCreated; }
    public void setTotalMawbsCreated(int totalMawbsCreated) { this.totalMawbsCreated = totalMawbsCreated; }
    public int getTotalBookingsCreated() { return totalBookingsCreated; }
    public void setTotalBookingsCreated(int totalBookingsCreated) { this.totalBookingsCreated = totalBookingsCreated; }
    public int getTotalUldAwbsCreated() { return totalUldAwbsCreated; }
    public void setTotalUldAwbsCreated(int totalUldAwbsCreated) { this.totalUldAwbsCreated = totalUldAwbsCreated; }
    public List<LoadPlanningSheetImportResultDTO> getSheetResults() { return sheetResults; }
    public void setSheetResults(List<LoadPlanningSheetImportResultDTO> sheetResults) { this.sheetResults = sheetResults; }
}
