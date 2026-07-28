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
public class BookingEventListener {

    private static final Logger log = LoggerFactory.getLogger(BookingEventListener.class);

    private final NotificationService notificationService;
    private final EmailNotificationService emailNotificationService;
    private final AuthClient authClient;

    public BookingEventListener(NotificationService notificationService,
                                 EmailNotificationService emailNotificationService,
                                 AuthClient authClient) {
        this.notificationService = notificationService;
        this.emailNotificationService = emailNotificationService;
        this.authClient = authClient;
    }

    @RabbitHandler
    public void handleBookingConfirmed(ReceiptCreatedEvent event) {
        log.info("Received booking.confirmed event: {}", event);
        try {
            for (UserDTO user : authClient.getAllUsers()) {
                notificationService.createNotification(
                        user.getId(),
                        "EMAIL",
                        "Reserva Confirmada",
                        "Se ha confirmado una reserva en el sistema.",
                        "BOOKING",
                        null
                );
                if (user.getEmail() != null) {
                    emailNotificationService.sendBookingConfirmation(
                            user.getId(), user.getEmail(), "N/A", null);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process booking.confirmed event: {}", e.getMessage(), e);
        }
    }
}
