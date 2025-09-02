package org.example.Repository;

import org.example.Model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {

    // Basic finders
    List<ProductVariant> findByProductId(String productId);
    List<ProductVariant> findByProductIdOrderByVariantRankAsc(String productId);
    List<ProductVariant> findByTitleContainingIgnoreCase(String title);
    List<ProductVariant> findBySkuContainingIgnoreCase(String sku);
    Optional<ProductVariant> findBySku(String sku);
    Optional<ProductVariant> findByBarcode(String barcode);

    // Count operations
    long countByProductId(String productId);

    // Active variants (not soft deleted)
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.deletedAt IS NULL")
    List<ProductVariant> findByDeletedAtIsNull();

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.productId = :productId AND pv.deletedAt IS NULL ORDER BY pv.variantRank ASC")
    List<ProductVariant> findByProductIdAndDeletedAtIsNullOrderByVariantRankAsc(@Param("productId") String productId);

    // Find variants by product name
    @Query("SELECT pv FROM ProductVariant pv JOIN pv.product p WHERE p.title LIKE %:productName% AND pv.deletedAt IS NULL")
    List<ProductVariant> findVariantsByProductName(@Param("productName") String productName);

    // Find variants by product title (exact match helper)
    @Query("SELECT pv FROM ProductVariant pv JOIN pv.product p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :productTitle, '%'))")
    List<ProductVariant> findVariantsByProductTitle(@Param("productTitle") String productTitle);

    // Get max variant rank for a product (for ordering)
    @Query("SELECT MAX(pv.variantRank) FROM ProductVariant pv WHERE pv.productId = :productId")
    Integer getMaxVariantRankForProduct(@Param("productId") String productId);

    // Stock-related queries (placeholder implementations)
    // Note: These return placeholder values since actual inventory is in separate tables
    @Query("SELECT CAST(0 as INTEGER) FROM ProductVariant pv WHERE pv.product.title LIKE %:productName%")
    Integer getTotalStockByProductName(@Param("productName") String productName);

    // Count variants for a product (alternative to stock count)
    @Query("SELECT CAST(COUNT(pv) as INTEGER) FROM ProductVariant pv WHERE pv.product.title LIKE %:productName% AND pv.deletedAt IS NULL")
    Integer getVariantCountByProductName(@Param("productName") String productName);

    // Advanced queries for reporting
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.sku IS NULL OR pv.sku = ''")
    List<ProductVariant> findVariantsWithoutSku();

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.barcode IS NULL OR pv.barcode = ''")
    List<ProductVariant> findVariantsWithoutBarcode();

    // Find variants by multiple criteria
    @Query("SELECT pv FROM ProductVariant pv WHERE " +
            "(:productId IS NULL OR pv.productId = :productId) AND " +
            "(:title IS NULL OR LOWER(pv.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:sku IS NULL OR LOWER(pv.sku) LIKE LOWER(CONCAT('%', :sku, '%'))) AND " +
            "pv.deletedAt IS NULL")
    List<ProductVariant> findVariantsByCriteria(
            @Param("productId") String productId,
            @Param("title") String title,
            @Param("sku") String sku);

    // Bulk operations support
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.productId IN :productIds AND pv.deletedAt IS NULL")
    List<ProductVariant> findByProductIdInAndDeletedAtIsNull(@Param("productIds") List<String> productIds);

    // Statistics queries
    @Query("SELECT COUNT(pv) FROM ProductVariant pv WHERE pv.deletedAt IS NULL")
    long countActiveVariants();

    @Query("SELECT p.title, COUNT(pv) FROM ProductVariant pv JOIN pv.product p WHERE pv.deletedAt IS NULL GROUP BY p.id, p.title")
    List<Object[]> getVariantCountByProduct();
}