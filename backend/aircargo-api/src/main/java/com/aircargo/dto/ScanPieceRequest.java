package com.aircargo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScanPieceRequest {

    @NotNull(message = "uldId es requerido")
    private UUID uldId;

    @NotBlank(message = "awbNumber es requerido")
    private String awbNumber;

    /** Opcional: si el barcode era un HAWB */
    private String hawbNumber;

    /** BARCODE o MANUAL */
    private String source = "BARCODE";
}
