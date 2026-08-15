package com.aircargo.flightservice.service;

import com.aircargo.flightservice.dto.FlightDTO;
import com.aircargo.flightservice.entity.Flight;
import com.aircargo.flightservice.entity.FlightStatus;
import com.aircargo.flightservice.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceImplTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightServiceImpl service;

    private Flight sampleFlight() {
        Flight f = new Flight();
        f.setId(UUID.randomUUID());
        f.setFlightNumber("6R-501");
        f.setOrigin("SDQ");
        f.setDestination("MIA");
        f.setFlightDate(LocalDate.of(2026, 8, 20));
        f.setStatus(FlightStatus.SCHEDULED);
        return f;
    }

    @Test
    void getAll_filtersByAirlineIdOnly() {
        UUID airlineId = UUID.randomUUID();
        when(flightRepository.findByAirlineId(airlineId)).thenReturn(List.of(sampleFlight()));

        List<FlightDTO> result = service.getAll(airlineId, null, null, null);

        assertEquals(1, result.size());
        verify(flightRepository).findByAirlineId(airlineId);
        verify(flightRepository, never()).findAll();
    }

    @Test
    void getAll_returnsAll_whenNoFilters() {
        when(flightRepository.findAll()).thenReturn(List.of(sampleFlight(), sampleFlight()));

        List<FlightDTO> result = service.getAll(null, null, null, null);

        assertEquals(2, result.size());
        verify(flightRepository).findAll();
    }

    @Test
    void getById_mapsEntity() {
        Flight f = sampleFlight();
        when(flightRepository.findById(f.getId())).thenReturn(Optional.of(f));

        Optional<FlightDTO> result = service.getById(f.getId());

        assertTrue(result.isPresent());
        assertEquals(f.getFlightNumber(), result.get().getFlightNumber());
        assertEquals(f.getStatus(), result.get().getStatus());
    }

    @Test
    void create_savesAndReturns() {
        Flight f = sampleFlight();
        when(flightRepository.save(any(Flight.class))).thenReturn(f);

        FlightDTO dto = FlightDTO.fromEntity(f);
        FlightDTO result = service.create(dto);

        assertNotNull(result);
        assertEquals(f.getFlightNumber(), result.getFlightNumber());
        verify(flightRepository).save(any(Flight.class));
    }

    @Test
    void updateStatus_changesAndPersists() {
        Flight f = sampleFlight();
        f.setStatus(FlightStatus.SCHEDULED);
        when(flightRepository.findById(f.getId())).thenReturn(Optional.of(f));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<FlightDTO> result = service.updateStatus(f.getId(), FlightStatus.DEPARTED);

        assertTrue(result.isPresent());
        assertEquals(FlightStatus.DEPARTED, result.get().getStatus());
    }

    @Test
    void update_ignoresNullFields() {
        Flight existing = sampleFlight();
        existing.setFlightNumber("OLD-100");
        when(flightRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        FlightDTO dto = FlightDTO.builder().flightNumber("NEW-200").build();
        Optional<FlightDTO> result = service.update(existing.getId(), dto);

        assertTrue(result.isPresent());
        assertEquals("NEW-200", result.get().getFlightNumber());
        assertEquals("SDQ", result.get().getOrigin());
    }

    @Test
    void getByAirlineIdAndFlightNumber_returnsFirstMatch() {
        Flight f = sampleFlight();
        UUID airlineId = UUID.randomUUID();
        when(flightRepository.findByAirlineIdAndFlightNumber(airlineId, "6R-501"))
                .thenReturn(List.of(f));

        Optional<FlightDTO> result = service.getByAirlineIdAndFlightNumber(airlineId, "6R-501");

        assertTrue(result.isPresent());
        assertEquals("6R-501", result.get().getFlightNumber());
    }

    @Test
    void delete_returnsFalse_whenNotExists() {
        UUID id = UUID.randomUUID();
        when(flightRepository.existsById(id)).thenReturn(false);

        assertFalse(service.delete(id));
        verify(flightRepository, never()).deleteById(any());
    }

    @Test
    void delete_returnsTrue_andDeletes_whenExists() {
        UUID id = UUID.randomUUID();
        when(flightRepository.existsById(id)).thenReturn(true);

        assertTrue(service.delete(id));
        verify(flightRepository).deleteById(id);
    }
}
