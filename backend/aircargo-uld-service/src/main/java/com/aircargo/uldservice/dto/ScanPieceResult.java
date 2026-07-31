package com.aircargo.uldservice.dto;

public class ScanPieceResult {
    private boolean success;
    private String message;
    private int pieceNumber;
    private String awbNumber;
    private String mawbId;
    private int totalOnUld;
    private int availablePieces;
    private String error;
    private String warning;

    public ScanPieceResult() {}

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getPieceNumber() { return pieceNumber; }
    public void setPieceNumber(int pieceNumber) { this.pieceNumber = pieceNumber; }
    public String getAwbNumber() { return awbNumber; }
    public void setAwbNumber(String awbNumber) { this.awbNumber = awbNumber; }
    public String getMawbId() { return mawbId; }
    public void setMawbId(String mawbId) { this.mawbId = mawbId; }
    public int getTotalOnUld() { return totalOnUld; }
    public void setTotalOnUld(int totalOnUld) { this.totalOnUld = totalOnUld; }
    public int getAvailablePieces() { return availablePieces; }
    public void setAvailablePieces(int availablePieces) { this.availablePieces = availablePieces; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }
}
