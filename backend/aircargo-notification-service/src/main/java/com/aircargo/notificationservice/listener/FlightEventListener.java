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
public class FlightEventListener {

    private static final Logger log = LoggerFactory.getLogger(FlightEventListener.class);

    private final NotificationService notificationService;
    private final EmailNotificationService emailNotificationService;
    private final AuthClient authClient;

    public FlightEventListener(NotificationService notificationService,
                                EmailNotificationService emailNotificationService,
                                AuthClient authClient) {
        this.notificationService = notificationService;
        this.emailNotificationService = emailNotificationService;
        this.authClient = authClient;
    }

    @RabbitHandler
    public void handleFlightDeparted(ReceiptCreatedEvent event) {
        log.info("Received flight.departed event: {}", event);
        try {
            for (UserDTO user : authClient.getAllUsers()) {
                notificationService.createNotification(
                        user.getId(),
                        "EMAIL",
                        "Vuelo Ha Partido",
                        "Un vuelo ha partido del aeropuerto.",
                        "FLIGHT",
                        null
                );
                if (user.getEmail() != null) {
                    emailNotificationService.sendFlightDepartedNotification(
                            user.getId(), user.getEmail(), "N/A", null);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process flight.departed event: {}", e.getMessage(), e);
        }
    }

    @RabbitHandler
    public void handleMawbStatusChanged(ReceiptCreatedEvent event) {
        log.info("Received mawb.status.changed event: {}", event);
        try {
            for (UserDTO user : authClient.getAllUsers()) {
                notificationService.createNotification(
                        user.getId(),
                        "EMAIL",
                        "Estado MAWB Actualizado",
                        "El estado de un conocimiento aéreo ha cambiado.",
                        "MAWB",
                        null
                );
                if (user.getEmail() != null) {
                    emailNotificationService.sendMawbStatusChanged(
                            user.getId(), user.getEmail(), "N/A", "ACTUALIZADO");
                }
            }
        } catch (Exception e) {
            log.error("Failed to process mawb.status.changed event: {}", e.getMessage(), e);
        }
    }
}
