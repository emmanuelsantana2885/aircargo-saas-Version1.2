package com.aircargo.feign.client;

import com.aircargo.feign.dto.MawbDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "mawb-service", url = "${mawb-service.url:http://localhost:9095}")
public interface MawbClient {

    @GetMapping("/api/cargo/mawbs/{id}")
    MawbDTO getMawbById(@PathVariable UUID id);

    @GetMapping("/api/cargo/mawbs/awb/{awbNumber}")
    MawbDTO getMawbByAwbNumber(@PathVariable String awbNumber);

    @GetMapping("/api/cargo/mawbs/flight/{flightId}")
    List<MawbDTO> getMawbsByFlight(@PathVariable UUID flightId);

    @PostMapping("/api/cargo/mawbs")
    MawbDTO createMawb(@RequestBody MawbDTO dto);

    @PatchMapping("/api/cargo/mawbs/{id}/status")
    MawbDTO updateMawbStatus(@PathVariable UUID id, @RequestBody String status);
}
