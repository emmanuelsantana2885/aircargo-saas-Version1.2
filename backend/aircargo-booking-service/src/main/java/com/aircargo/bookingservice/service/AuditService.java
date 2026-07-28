package com.aircargo.bookingservice.service;

import com.aircargo.bookingservice.event.AuditLogEvent;
import com.aircargo.common.util.TextUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange:aircargo.events}")
    private String exchange;

    public AuditService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void log(UUID userId, String email, String fullName, String action,
                    String entityType, String entityId, String details,
                    String ipAddress) {
        try {
            var event = new AuditLogEvent(
                    userId, email, fullName, action, entityType, entityId,
                    TextUtil.safe(details), ipAddress
            );
            rabbitTemplate.convertAndSend(exchange, "audit.log", event);
        } catch (Exception e) {
            // Non-blocking - log failure shouldn't break business logic
        }
    }

    public void logLogin(UUID userId, String email, String fullName, String ipAddress) {
        log(userId, email, fullName, "LOGIN", "USER", userId.toString(), null, ipAddress);
    }
}