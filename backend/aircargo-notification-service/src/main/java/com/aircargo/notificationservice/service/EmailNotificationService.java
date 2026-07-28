package com.aircargo.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    public void sendEmail(UUID userId, String to, String subject, String body) {
        log.info("Sending email to user={} address={}: subject={}", userId, to, subject);
        log.info("Email body: {}", body);
    }

    public void sendReceiptNotification(UUID userId, String to, String awbNumber, UUID receiptId) {
        String subject = "AirCargo — Recibo de Bodega AWB " + awbNumber;
        String body = "Se ha emitido un recibo de bodega para el conocimiento aéreo " + awbNumber
                + ". ID del recibo: " + receiptId;
        sendEmail(userId, to, subject, body);
    }

    public void sendBookingConfirmation(UUID userId, String to, String bookingNumber, UUID bookingId) {
        String subject = "AirCargo — Reserva Confirmada " + bookingNumber;
        String body = "La reserva " + bookingNumber + " ha sido confirmada. ID: " + bookingId;
        sendEmail(userId, to, subject, body);
    }

    public void sendFlightDepartedNotification(UUID userId, String to, String flightNumber, UUID flightId) {
        String subject = "AirCargo — Vuelo " + flightNumber + " Ha Partido";
        String body = "El vuelo " + flightNumber + " ha partido. ID: " + flightId;
        sendEmail(userId, to, subject, body);
    }

    public void sendMawbStatusChanged(UUID userId, String to, String awbNumber, String status) {
        String subject = "AirCargo — MAWB " + awbNumber + " Estado: " + status;
        String body = "El conocimiento aéreo " + awbNumber + " ha cambiado a estado: " + status;
        sendEmail(userId, to, subject, body);
    }
}
