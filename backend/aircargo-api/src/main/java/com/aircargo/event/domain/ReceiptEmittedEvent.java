package com.aircargo.event.domain;

import java.util.UUID;

public record ReceiptEmittedEvent(
    UUID receiptId,
    UUID mawbId,
    String mawbNumber,
    int totalPieces
) {}
