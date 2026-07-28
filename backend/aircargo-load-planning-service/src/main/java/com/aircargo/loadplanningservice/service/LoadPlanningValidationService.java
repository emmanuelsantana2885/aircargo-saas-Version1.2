package com.aircargo.loadplanningservice.service;

import com.aircargo.loadplanningservice.dto.FlightLoadPlanDTO;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;
import java.util.HashMap;

@Service
public class LoadPlanningValidationService {

    private static final Map<String, Double> POSITION_WEIGHT_LIMITS = new HashMap<>();

    static {
        POSITION_WEIGHT_LIMITS.put("P1", 5500.0);
        POSITION_WEIGHT_LIMITS.put("P2", 5500.0);
        POSITION_WEIGHT_LIMITS.put("P3", 5500.0);
        POSITION_WEIGHT_LIMITS.put("P4", 5500.0);
        POSITION_WEIGHT_LIMITS.put("P5", 5200.0);
        POSITION_WEIGHT_LIMITS.put("P6", 5200.0);
        POSITION_WEIGHT_LIMITS.put("P7", 5000.0);

        POSITION_WEIGHT_LIMITS.put("10L", 3200.0);
        POSITION_WEIGHT_LIMITS.put("10R", 3200.0);
        POSITION_WEIGHT_LIMITS.put("11L", 3200.0);
        POSITION_WEIGHT_LIMITS.put("11R", 3200.0);
        POSITION_WEIGHT_LIMITS.put("12L", 2800.0);
        POSITION_WEIGHT_LIMITS.put("12R", 2800.0);
        POSITION_WEIGHT_LIMITS.put("13",  3500.0);
    }

    public void validateStructuralLimits(FlightLoadPlanDTO uldData) {
        if (uldData.getPos() == null || uldData.getPos().trim().isEmpty()) {
            return;
        }

        String positionKey = uldData.getPos().toUpperCase().trim();
        Double maxAllowedPayload = POSITION_WEIGHT_LIMITS.get(positionKey);

        if (maxAllowedPayload == null) {
            throw new IllegalArgumentException("La posicion ingresada [" + positionKey + "] no existe en la configuracion de la aeronave.");
        }

        double grossWeight = uldData.getWeight() != null ? uldData.getWeight() : 0.0;
        double tareWeight = uldData.getTara() != null ? uldData.getTara() : 0.0;

        double netPayload = grossWeight - tareWeight;

        if (netPayload <= 0) {
            throw new IllegalArgumentException("Error de Bascula: El Gross Weight debe ser mayor a la Tara del ULD.");
        }

        if (netPayload > maxAllowedPayload) {
            double excess = netPayload - maxAllowedPayload;
            throw new StructuralOverloadException(
                String.format("ALERTA DE TOLERANCIA CRITICA: La posicion %s excede el limite por %.2f lbs. " +
                              "[Limite Max: %.0f lbs | Peso Neto Recibido: %.0f lbs]",
                              positionKey, excess, maxAllowedPayload, netPayload)
            );
        }
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public static class StructuralOverloadException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public StructuralOverloadException(String message) {
            super(message);
        }
    }
}
