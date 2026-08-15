package com.aircargo.bookingservice.service;

import com.aircargo.bookingservice.dto.BookingDTO;
import com.aircargo.bookingservice.entity.Booking;
import com.aircargo.bookingservice.entity.Flight;
import com.aircargo.bookingservice.repository.BookingRepository;
import com.aircargo.common.event.BookingAwbUpdatedEvent;
import com.aircargo.feign.client.FlightClient;
import com.aircargo.feign.client.MawbClient;
import com.aircargo.feign.dto.FlightDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    /**
     * RabbitTemplate cannot be instrumented by Mockito's inline mockmaker
     * under JDK 25 (restricted dynamic agent). Use a real subclass that
     * records convertAndSend calls instead of mocking.
     */
    static class FakeRabbitTemplate extends RabbitTemplate {
        String lastExchange;
        String lastRoutingKey;
        Object lastPayload;

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message) {
            this.lastExchange = exchange;
            this.lastRoutingKey = routingKey;
            this.lastPayload = message;
        }
    }

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private FlightClient flightClient;
    @Mock
    private MawbClient mawbClient;

    private FakeRabbitTemplate rabbitTemplate;
    private BookingServiceImpl service;

    @BeforeEach
    void setUp() {
        rabbitTemplate = new FakeRabbitTemplate();
        service = new BookingServiceImpl(bookingRepository, flightClient, mawbClient, rabbitTemplate);
        ReflectionTestUtils.setField(service, "exchange", "aircargo.events");
    }

    private Booking sampleBooking() {
        Flight flight = new Flight();
        flight.setId(UUID.randomUUID());
        return Booking.builder()
                .id(UUID.randomUUID())
                .clientName("Rannik Cargo")
                .flight(flight)
                .build();
    }

    @Test
    void create_derivesAirlineFromFlight_whenMissing() {
        UUID flightId = UUID.randomUUID();
        UUID airlineId = UUID.randomUUID();
        FlightDTO flightDto = new FlightDTO();
        flightDto.setAirlineId(airlineId);
        when(flightClient.getFlightById(flightId)).thenReturn(flightDto);

        BookingDTO dto = BookingDTO.builder()
                .clientName("Rannik Cargo")
                .flightId(flightId)
                .build();
        Booking saved = sampleBooking();
        when(bookingRepository.save(any(Booking.class))).thenReturn(saved);

        BookingDTO result = service.create(dto);

        assertNotNull(result);
        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertNotNull(captor.getValue().getAirline());
        assertEquals(airlineId, captor.getValue().getAirline().getId());
    }

    @Test
    void create_doesNotFail_whenFlightServiceUnavailable() {
        UUID flightId = UUID.randomUUID();
        when(flightClient.getFlightById(flightId)).thenThrow(new RuntimeException("flight down"));
        Booking saved = sampleBooking();
        when(bookingRepository.save(any(Booking.class))).thenReturn(saved);

        BookingDTO dto = BookingDTO.builder().clientName("X").flightId(flightId).build();

        assertDoesNotThrow(() -> service.create(dto));
    }

    @Test
    void update_capsFulfillmentPctAt9999_9999() {
        Booking existing = sampleBooking();
        when(bookingRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingDTO dto = BookingDTO.builder()
                .fulfillmentPct(BigDecimal.valueOf(12000.0000))
                .build();

        Optional<BookingDTO> result = service.update(existing.getId(), dto);

        assertTrue(result.isPresent());
        assertEquals(0, result.get().getFulfillmentPct().compareTo(BigDecimal.valueOf(9999.9999)));
    }

    @Test
    void updateAwb_setsAwbAndPublishesEvent() {
        Booking existing = sampleBooking();
        when(bookingRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<BookingDTO> result = service.updateAwb(existing.getId(), "406-05912970");

        assertTrue(result.isPresent());
        assertEquals("406-05912970", result.get().getAwbNumber());

        assertEquals("aircargo.events", rabbitTemplate.lastExchange);
        assertEquals("booking.awb.updated", rabbitTemplate.lastRoutingKey);
        assertTrue(rabbitTemplate.lastPayload instanceof BookingAwbUpdatedEvent);
        BookingAwbUpdatedEvent event = (BookingAwbUpdatedEvent) rabbitTemplate.lastPayload;
        assertEquals("406-05912970", event.awbNumber());
        assertEquals(existing.getFlight().getId(), event.flightId());
    }

    @Test
    void findByMawbId_returnsFirstBooking() {
        Booking b = sampleBooking();
        UUID mawbId = UUID.randomUUID();
        when(bookingRepository.findByMawbId(mawbId)).thenReturn(List.of(b));

        Optional<BookingDTO> result = service.findByMawbId(mawbId);

        assertTrue(result.isPresent());
        assertEquals(b.getClientName(), result.get().getClientName());
    }

    @Test
    void delete_returnsFalse_whenNotExists() {
        UUID id = UUID.randomUUID();
        when(bookingRepository.existsById(id)).thenReturn(false);

        assertFalse(service.delete(id));
        verify(bookingRepository, never()).deleteById(any());
    }
}
