package org.example.Repository;

import org.example.Model.InventoryLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryLevelRepository extends JpaRepository<InventoryLevel, String> {

    // Change return type from Integer to BigDecimal
    @Query("SELECT COALESCE(SUM(il.stockedQuantity - il.reservedQuantity + il.incomingQuantity), 0) " +
            "FROM InventoryLevel il " +
            "WHERE il.inventoryItem.title LIKE :productName AND il.deletedAt IS NULL")
    BigDecimal getTotalAvailableStockByProductName(@Param("productName") String productName);

    // Also fix this method's parameter type
    @Query("SELECT il FROM InventoryLevel il " +
            "WHERE (il.stockedQuantity - il.reservedQuantity + il.incomingQuantity) < :threshold " +
            "AND il.deletedAt IS NULL")
    List<InventoryLevel> findLowStockItems(@Param("threshold") BigDecimal threshold);
    @Query("SELECT il FROM InventoryLevel il " +
            "WHERE il.inventoryItem.id = :inventoryItemId " +
            "AND il.deletedAt IS NULL")
    Optional<InventoryLevel> findActiveByInventoryItemId(@Param("inventoryItemId") String inventoryItemId);

}