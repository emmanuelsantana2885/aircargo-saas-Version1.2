package com.aircargo.service;

import com.aircargo.dto.UldAwbDTO;
import com.aircargo.entity.Mawb;
import com.aircargo.entity.MawbStatus;
import com.aircargo.entity.Uld;
import com.aircargo.entity.UldAwb;
import com.aircargo.entity.WarehouseReceipt;
import com.aircargo.repository.MawbRepository;
import com.aircargo.repository.UldAwbRepository;
import com.aircargo.repository.UldRepository;
import com.aircargo.repository.WarehouseReceiptRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UldAwbServiceImpl implements UldAwbService {

    private final UldAwbRepository uldAwbRepository;
    private final UldRepository uldRepository;
    private final MawbRepository mawbRepository;
    private final WarehouseReceiptRepository receiptRepository;

    public UldAwbServiceImpl(UldAwbRepository uldAwbRepository,
                              UldRepository uldRepository,
                              MawbRepository mawbRepository,
                              WarehouseReceiptRepository receiptRepository) {
        this.uldAwbRepository = uldAwbRepository;
        this.uldRepository = uldRepository;
        this.mawbRepository = mawbRepository;
        this.receiptRepository = receiptRepository;
    }

    @Override
    public List<UldAwbDTO> getAll(UUID uldId, UUID mawbId) {
        List<UldAwb> results;
        if (uldId != null) {
            results = uldAwbRepository.findByUldId(uldId);
        } else if (mawbId != null) {
            results = uldAwbRepository.findByMawbId(mawbId);
        } else {
            results = uldAwbRepository.findAll();
        }
        return results.stream()
                .map(UldAwbDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UldAwbDTO> getById(UUID id) {
        return uldAwbRepository.findById(id)
                .map(UldAwbDTO::fromEntity);
    }

    @Override
    public UldAwbDTO create(UldAwbDTO dto) {
        // Validar límite de piezas montadas vs reservadas/recibidas
        if (dto.getMawbId() != null && dto.getPieces() != null) {
            mawbRepository.findById(dto.getMawbId()).ifPresent(mawb -> {
                int existingPieces = uldAwbRepository.findByMawbId(mawb.getId()).stream()
                        .mapToInt(link -> link.getPieces() != null ? link.getPieces() : 0)
                        .sum();
                int newPieces = dto.getPieces();
                int totalPieces = existingPieces + newPieces;
                int reservedPieces = mawb.getPieces() != null ? mawb.getPieces() : 0;

                int receivedPieces = receiptRepository.findByMawbId(mawb.getId()).stream()
                        .mapToInt(r -> r.getPieceCount() != null ? r.getPieceCount() : 0)
                        .sum();

                int maxAllowed;
                if (mawb.getStatus() == MawbStatus.RECEIVED || mawb.getStatus() == MawbStatus.MANIFESTED || mawb.getStatus() == MawbStatus.DEPARTED) {
                    maxAllowed = Math.max(reservedPieces, receivedPieces);
                } else {
                    maxAllowed = reservedPieces;
                }

                if (totalPieces > maxAllowed && maxAllowed > 0) {
                    throw new IllegalArgumentException(
                        "Límite de piezas excedido para MAWB " + mawb.getAwbNumber() +
                        ": tiene " + existingPieces + " montadas, intenta agregar " + newPieces +
                        ", máximo permitido: " + maxAllowed +
                        " (reservadas: " + reservedPieces + ", recibidas: " + receivedPieces + ")"
                    );
                }

                // Auto-actualizar estado a MANIFESTED si está BOOKED o RECEIVED
                if (mawb.getStatus() == MawbStatus.BOOKED || mawb.getStatus() == MawbStatus.RECEIVED) {
                    mawb.setStatus(MawbStatus.MANIFESTED);
                    mawbRepository.save(mawb);
                }
            });
        }

        UldAwb entity = buildEntity(dto, new UldAwb());
        entity.setId(null);
        UldAwb saved = uldAwbRepository.save(entity);
        return UldAwbDTO.fromEntity(saved);
    }

    @Override
    public Optional<UldAwbDTO> update(UUID id, UldAwbDTO dto) {
        return uldAwbRepository.findById(id)
                .map(existing -> {
                    UldAwb updated = buildEntity(dto, existing);
                    updated.setId(existing.getId());
                    return uldAwbRepository.save(updated);
                })
                .map(UldAwbDTO::fromEntity);
    }

    @Override
    public boolean delete(UUID id) {
        if (!uldAwbRepository.existsById(id)) return false;
        uldAwbRepository.deleteById(id);
        return true;
    }

    /**
     * Resuelve uld y mawb (si vienen) contra la BD y mapea el resto de campos del DTO.
     * Requisito de negocio: un uld_awb necesita uld_id siempre; mawb_id es opcional
     * (puede ser mawb_label para casos como EMPTY ULD, RED TAG, WWEF, etc.)
     */
    private UldAwb buildEntity(UldAwbDTO dto, UldAwb target) {
        Uld uld = uldRepository.findById(dto.getUldId())
                .orElseThrow(() -> new IllegalArgumentException("Uld not found: " + dto.getUldId()));
        target.setUld(uld);

        if (dto.getMawbId() != null) {
            Mawb mawb = mawbRepository.findById(dto.getMawbId())
                    .orElseThrow(() -> new IllegalArgumentException("Mawb not found: " + dto.getMawbId()));
            target.setMawb(mawb);
        } else {
            target.setMawb(null);
        }

        target.setMawbLabel(dto.getMawbLabel());
        target.setDescription(dto.getDescription());
        target.setDestination(dto.getDestination());
        target.setPieces(dto.getPieces());
        target.setPiecesPct(dto.getPiecesPct());
        target.setTempInbound(dto.getTempInbound());
        target.setTempOutbound(dto.getTempOutbound());
        target.setHc(dto.getHc());
        target.setComments(dto.getComments());
        target.setConsumptionPallets(dto.getConsumptionPallets());
        target.setStartTime(dto.getStartTime());
        target.setEndTime(dto.getEndTime());
        target.setAvgTimePerPieceSec(dto.getAvgTimePerPieceSec());
        return target;
    }
}
