package com.aircargo.uldservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ScanPieceRequest {

    @NotNull
    private UUID uldId;

    @NotBlank
    private String awbNumber;

    private String hawbNumber;

    private String source = "BARCODE";

    public ScanPieceRequest() {}

    public UUID getUldId() { return uldId; }
    public void setUldId(UUID uldId) { this.uldId = uldId; }
    public String getAwbNumber() { return awbNumber; }
    public void setAwbNumber(String awbNumber) { this.awbNumber = awbNumber; }
    public String getHawbNumber() { return hawbNumber; }
    public void setHawbNumber(String hawbNumber) { this.hawbNumber = hawbNumber; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
