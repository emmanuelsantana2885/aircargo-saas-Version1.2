package com.aircargo.uldservice.dto;

public class ScanLookupDTO {
    private String type;
    private String awbNumber;
    private String mawbId;
    private String shipperName;
    private String consigneeName;
    private String commodityType;
    private String destination;
    private int reservedPieces;
    private int receivedPieces;
    private int assignedTotal;
    private int availablePieces;
    private int existingOnUld;
    private String hawbNumber;
    private Integer hawbPieces;
    private String uldId;
    private String uldNumber;
    private String uldType;
    private String flightId;
    private String status;
    private int currentPieces;

    public ScanLookupDTO() {}

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAwbNumber() { return awbNumber; }
    public void setAwbNumber(String awbNumber) { this.awbNumber = awbNumber; }
    public String getMawbId() { return mawbId; }
    public void setMawbId(String mawbId) { this.mawbId = mawbId; }
    public String getShipperName() { return shipperName; }
    public void setShipperName(String shipperName) { this.shipperName = shipperName; }
    public String getConsigneeName() { return consigneeName; }
    public void setConsigneeName(String consigneeName) { this.consigneeName = consigneeName; }
    public String getCommodityType() { return commodityType; }
    public void setCommodityType(String commodityType) { this.commodityType = commodityType; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public int getReservedPieces() { return reservedPieces; }
    public void setReservedPieces(int reservedPieces) { this.reservedPieces = reservedPieces; }
    public int getReceivedPieces() { return receivedPieces; }
    public void setReceivedPieces(int receivedPieces) { this.receivedPieces = receivedPieces; }
    public int getAssignedTotal() { return assignedTotal; }
    public void setAssignedTotal(int assignedTotal) { this.assignedTotal = assignedTotal; }
    public int getAvailablePieces() { return availablePieces; }
    public void setAvailablePieces(int availablePieces) { this.availablePieces = availablePieces; }
    public int getExistingOnUld() { return existingOnUld; }
    public void setExistingOnUld(int existingOnUld) { this.existingOnUld = existingOnUld; }
    public String getHawbNumber() { return hawbNumber; }
    public void setHawbNumber(String hawbNumber) { this.hawbNumber = hawbNumber; }
    public Integer getHawbPieces() { return hawbPieces; }
    public void setHawbPieces(Integer hawbPieces) { this.hawbPieces = hawbPieces; }
    public String getUldId() { return uldId; }
    public void setUldId(String uldId) { this.uldId = uldId; }
    public String getUldNumber() { return uldNumber; }
    public void setUldNumber(String uldNumber) { this.uldNumber = uldNumber; }
    public String getUldType() { return uldType; }
    public void setUldType(String uldType) { this.uldType = uldType; }
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getCurrentPieces() { return currentPieces; }
    public void setCurrentPieces(int currentPieces) { this.currentPieces = currentPieces; }
}
