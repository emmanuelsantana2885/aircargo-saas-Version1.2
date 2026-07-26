package com.aircargo.repository;

import com.aircargo.entity.WarehouseReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseReceiptRepository extends JpaRepository<WarehouseReceipt, UUID> {
    
    /**
     * Recupera los recibos de bodega asociados a una aerolínea (Aislamiento Multi-tenant).
     */
    List<WarehouseReceipt> findByAirlineId(UUID airlineId);

    /**
     * Recupera todos los recibos NO superseded de una aerolínea.
     */
    List<WarehouseReceipt> findByAirlineIdAndSupersededFalse(UUID airlineId);

    /**
     * Recupera todos los recibos de bodega asociados a una MAWB.
     */
    List<WarehouseReceipt> findByMawbId(UUID mawbId);

    /**
     * Recupera todos los recibos NO superseded (el GET principal usa esto).
     */
    List<WarehouseReceipt> findBySupersededFalse();

    List<WarehouseReceipt> findByMawbIdOrderByCreatedAtAsc(UUID mawbId);

    /**
     * Recupera solo los recibos NO superseded de una MAWB.
     */
    List<WarehouseReceipt> findByMawbIdAndSupersededFalse(UUID mawbId);

    /**
     * Busca un recibo de bodega para un MAWB y HAWB específicos.
     * Usado para detectar si ya existe un recibo para este HAWB y
     * actualizarlo en lugar de insertar uno nuevo.
     */
    Optional<WarehouseReceipt> findByMawbIdAndHawbId(UUID mawbId, UUID hawbId);

    List<WarehouseReceipt> findByMawbIdAndHawbIdIsNotNull(UUID mawbId);

    /**
     * Marca como superseded todos los recibos de una MAWB excepto el más reciente.
     * Retorna el ID del recibo que sobrevive (el más reciente).
     */
    @Query(value = "UPDATE warehouse_receipt SET superseded = true " +
            "WHERE mawb_id = :mawbId AND id <> :keepId " +
            "AND superseded = false " +
            "RETURNING id", nativeQuery = true)
    List<UUID> supersedeOthers(@Param("mawbId") UUID mawbId, @Param("keepId") UUID keepId);
}
