package com.aircargo.notificationservice.listener;

import com.aircargo.common.event.ReceiptCreatedEvent;
import com.aircargo.feign.client.AuthClient;
import com.aircargo.feign.dto.UserDTO;
import com.aircargo.notificationservice.service.EmailNotificationService;
import com.aircargo.notificationservice.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "aircargo.notifications")
public class ReceiptEventListener {

    private static final Logger log = LoggerFactory.getLogger(ReceiptEventListener.class);

    private final NotificationService notificationService;
    private final EmailNotificationService emailNotificationService;
    private final AuthClient authClient;

    public ReceiptEventListener(NotificationService notificationService,
                                 EmailNotificationService emailNotificationService,
                                 AuthClient authClient) {
        this.notificationService = notificationService;
        this.emailNotificationService = emailNotificationService;
        this.authClient = authClient;
    }

    @RabbitHandler
    public void handleReceiptCreated(ReceiptCreatedEvent event) {
        log.info("Received receipt.created event: receiptId={}, mawbId={}, awbNumber={}",
                event.receiptId(), event.mawbId(), event.mawbNumber());
        try {
            for (UserDTO user : authClient.getAllUsers()) {
                notificationService.createNotification(
                        user.getId(),
                        "EMAIL",
                        "Recibo de Bodega Emitido",
                        "Se ha emitido un recibo de bodega para el AWB " + event.mawbNumber()
                                + ". ID del recibo: " + event.receiptId(),
                        "RECEIPT",
                        event.receiptId()
                );
                if (user.getEmail() != null) {
                    emailNotificationService.sendReceiptNotification(
                            user.getId(), user.getEmail(), event.mawbNumber(), event.receiptId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to process receipt.created event: {}", e.getMessage(), e);
        }
    }
}
