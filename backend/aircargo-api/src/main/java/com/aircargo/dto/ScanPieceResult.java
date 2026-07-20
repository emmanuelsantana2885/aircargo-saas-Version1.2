package com.aircargo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScanPieceResult {

    private boolean success;
    private String message;
    private int pieceNumber;
    private String awbNumber;
    private int totalOnUld;
    private int availablePieces;
    private String error;
}
