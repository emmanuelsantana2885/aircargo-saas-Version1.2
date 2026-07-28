package com.aircargo.loadplanningservice.dto;

import java.util.List;
import java.util.UUID;

public class LoadPlanningSheetImportResultDTO {
    private String sheetName;
    private UUID flightId;
    private String flightNumber;
    private boolean success;
    private String error;
    private int uldsCreated;
    private int uldsUpdated;
    private int mawbsCreated;
    private int bookingsCreated;
    private int uldAwbsCreated;
    private List<String> warnings;

    public LoadPlanningSheetImportResultDTO() {}

    public String getSheetName() { return sheetName; }
    public void setSheetName(String sheetName) { this.sheetName = sheetName; }
    public UUID getFlightId() { return flightId; }
    public void setFlightId(UUID flightId) { this.flightId = flightId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public int getUldsCreated() { return uldsCreated; }
    public void setUldsCreated(int uldsCreated) { this.uldsCreated = uldsCreated; }
    public int getUldsUpdated() { return uldsUpdated; }
    public void setUldsUpdated(int uldsUpdated) { this.uldsUpdated = uldsUpdated; }
    public int getMawbsCreated() { return mawbsCreated; }
    public void setMawbsCreated(int mawbsCreated) { this.mawbsCreated = mawbsCreated; }
    public int getBookingsCreated() { return bookingsCreated; }
    public void setBookingsCreated(int bookingsCreated) { this.bookingsCreated = bookingsCreated; }
    public int getUldAwbsCreated() { return uldAwbsCreated; }
    public void setUldAwbsCreated(int uldAwbsCreated) { this.uldAwbsCreated = uldAwbsCreated; }
    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}
