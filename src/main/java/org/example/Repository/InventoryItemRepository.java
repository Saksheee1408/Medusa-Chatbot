package org.example.Repository;

import org.example.Model.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, String> {

    List<InventoryItem> findByTitleContainingIgnoreCase(String title);

    List<InventoryItem> findBySku(String sku);

    List<InventoryItem> findByDeletedAtIsNull();

    @Query("SELECT ii FROM InventoryItem ii WHERE ii.sku = :sku AND ii.deletedAt IS NULL")
    Optional<InventoryItem> findActiveBySku(@Param("sku") String sku);

    @Query("SELECT DISTINCT ii FROM InventoryItem ii " +
            "JOIN ii.inventoryLevels il " +
            "WHERE (il.stockedQuantity - il.reservedQuantity + il.incomingQuantity) > :minStock " +
            "AND ii.deletedAt IS NULL " +
            "AND il.deletedAt IS NULL")
    List<InventoryItem> findItemsWithStockGreaterThan(@Param("minStock") BigDecimal minStock);

}
