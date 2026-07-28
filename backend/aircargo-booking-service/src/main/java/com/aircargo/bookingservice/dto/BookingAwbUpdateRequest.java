package com.aircargo.bookingservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingAwbUpdateRequest {
    @NotBlank(message = "AWB number is required")
    private String awbNumber;
}