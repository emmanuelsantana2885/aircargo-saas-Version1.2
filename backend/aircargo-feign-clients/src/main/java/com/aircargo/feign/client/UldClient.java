package com.aircargo.feign.client;

import com.aircargo.feign.dto.UldDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "uld-service", url = "${uld-service.url:http://localhost:9097}")
public interface UldClient {

    @GetMapping("/api/ulds/{id}")
    UldDTO getUldById(@PathVariable UUID id);

    @GetMapping("/api/ulds/flight/{flightId}")
    List<UldDTO> getUldsByFlight(@PathVariable UUID flightId);
}
