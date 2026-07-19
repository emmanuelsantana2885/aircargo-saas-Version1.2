package com.aircargo.event;

import com.aircargo.common.event.ReceiptCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class ReceiptEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReceiptEventListener.class);

    @Async
    @EventListener
    public void handleReceiptCreated(ReceiptCreatedEvent event) {
        log.info("Async: background processing started for receipt {}", event.receiptId());
        try {
            Thread.sleep(100);
            log.info("Async: background processing completed for receipt {}", event.receiptId());
        } catch (Exception e) {
            log.error("Async: background processing failed for receipt {}", event.receiptId(), e);
        }
    }
}