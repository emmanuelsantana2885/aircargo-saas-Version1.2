package com.aircargo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScanLookupDTO {

    /** "MAWB", "HAWB", o "ULD" */
    private String type;

    // -- Si es MAWB o HAWB --
    private String awbNumber;
    private String mawbId;
    private String shipperName;
    private String consigneeName;
    private String commodityType;
    private String destination;
    private int reservedPieces;
    private int receivedPieces;
    private int assignedTotal;
    private int availablePieces;
    private int existingOnUld;

    // -- Si es HAWB --
    private String hawbNumber;
    private Integer hawbPieces;

    // -- Si es ULD --
    private String uldId;
    private String uldNumber;
    private String uldType;
    private String flightId;
    private String status;
    private int currentPieces;
}
