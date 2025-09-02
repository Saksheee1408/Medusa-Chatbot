package org.example.Repository;

import org.example.Model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, String> {

    List<Price> findByProductId(String productId);

    List<Price> findByVariantId(String variantId);

    List<Price> findByPriceListId(String priceListId);

    @Query("SELECT p FROM Price p WHERE p.productId = :productId AND p.priceList.status = 'active'")
    List<Price> findActiveProductPrices(@Param("productId") String productId);

    @Query("SELECT p FROM Price p WHERE p.variantId = :variantId AND p.priceList.status = 'active'")
    List<Price> findActiveVariantPrices(@Param("variantId") String variantId);

    @Query("SELECT p FROM Price p WHERE p.amount BETWEEN :minPrice AND :maxPrice")
    List<Price> findPricesInRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM Price p JOIN p.priceList pl WHERE " +
            "(p.productId = :productId OR p.variantId = :variantId) AND " +
            "pl.status = 'active' AND " +
            "(pl.startsAt IS NULL OR pl.startsAt <= CURRENT_TIMESTAMP) AND " +
            "(pl.endsAt IS NULL OR pl.endsAt >= CURRENT_TIMESTAMP) " +
            "ORDER BY p.amount ASC")
    List<Price> findCurrentPricesForItem(@Param("productId") String productId, @Param("variantId") String variantId);
}