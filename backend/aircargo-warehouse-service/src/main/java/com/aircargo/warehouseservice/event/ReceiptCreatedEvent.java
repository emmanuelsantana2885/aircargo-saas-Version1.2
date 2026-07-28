package com.aircargo.warehouseservice.event;

import java.util.UUID;

public class ReceiptCreatedEvent {
    private UUID receiptId;
    private UUID mawbId;
    private String awbNumber;

    public ReceiptCreatedEvent() {}

    public ReceiptCreatedEvent(UUID receiptId, UUID mawbId, String awbNumber) {
        this.receiptId = receiptId;
        this.mawbId = mawbId;
        this.awbNumber = awbNumber;
    }

    public UUID getReceiptId() { return receiptId; }
    public void setReceiptId(UUID receiptId) { this.receiptId = receiptId; }
    public UUID getMawbId() { return mawbId; }
    public void setMawbId(UUID mawbId) { this.mawbId = mawbId; }
    public String getAwbNumber() { return awbNumber; }
    public void setAwbNumber(String awbNumber) { this.awbNumber = awbNumber; }
}