package com.aircargo.flightservice.dto;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FeignContractSyncTest {

    private Set<String> names(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }

    private void assertContract(Class<?> wire, Class<?> local, String label) {
        Set<String> missing = names(wire).stream()
                .filter(f -> !names(local).contains(f))
                .collect(Collectors.toSet());
        assertTrue(missing.isEmpty(), () -> label + ": campos del contrato feign ausentes en el DTO del servicio: " + missing);
    }

    @Test
    void flightDto_contractIsSubsetOfServiceDto() {
        assertContract(com.aircargo.feign.dto.FlightDTO.class, FlightDTO.class, "FlightDTO");
    }

    @Test
    void airlineDto_contractIsSubsetOfServiceDto() {
        assertContract(com.aircargo.feign.dto.AirlineDTO.class, AirlineDTO.class, "AirlineDTO");
    }
}
