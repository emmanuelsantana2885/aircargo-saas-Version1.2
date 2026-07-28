package com.aircargo.flightservice.dto;

import com.aircargo.common.entity.Airline;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirlineDTO {

    private UUID id;
    private String code;
    private String name;
    private String iataCode;
    private String country;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static AirlineDTO fromEntity(Airline airline) {
        if (airline == null) return null;
        return AirlineDTO.builder()
                .id(airline.getId())
                .code(airline.getCode())
                .name(airline.getName())
                .iataCode(airline.getIataCode())
                .country(airline.getCountry())
                .isActive(airline.getIsActive())
                .createdAt(airline.getCreatedAt())
                .updatedAt(airline.getUpdatedAt())
                .build();
    }

    public static Airline toEntity(AirlineDTO dto) {
        if(dto == null) return null;
        Airline entity = new Airline();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setIataCode(dto.getIataCode());
        entity.setCountry(dto.getCountry());
        entity.setIsActive(dto.getIsActive());
        return entity;
    }
}