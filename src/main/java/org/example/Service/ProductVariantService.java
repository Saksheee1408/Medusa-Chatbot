package org.example.Service;

import org.example.Model.Product;
import org.example.Model.ProductVariant;
import org.example.Repository.ProductRepository;
import org.example.Repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductVariantService {

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductRepository productRepository;

    // CREATE operations
    public ProductVariant createVariant(String productId, String title, String sku, String barcode) {
        // Validate product exists
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            throw new IllegalArgumentException("Product with ID " + productId + " not found");
        }

        ProductVariant variant = new ProductVariant();

        // Generate variant ID
        String uuid = UUID.randomUUID().toString().replace("-", "");
        variant.setId("variant_" + uuid.substring(0, 8));

        variant.setTitle(title);
        variant.setProductId(productId);
        variant.setSku(sku);
        variant.setBarcode(barcode);

        // Set defaults
        variant.setAllowBackorder(false);
        variant.setManageInventory(true);
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());

        // Calculate variant rank (next position)
        Integer maxRank = variantRepository.getMaxVariantRankForProduct(productId);
        variant.setVariantRank(maxRank != null ? maxRank + 1 : 1);

        return variantRepository.save(variant);
    }

    public ProductVariant createVariant(String productId, ProductVariant variant) {
        // Validate product exists
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            throw new IllegalArgumentException("Product with ID " + productId + " not found");
        }

        // Generate variant ID if not provided
        if (variant.getId() == null || variant.getId().isEmpty()) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            variant.setId("variant_" + uuid.substring(0, 8));
        }

        variant.setProductId(productId);
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());

        // Set default values if not provided
        if (variant.getAllowBackorder() == null) {
            variant.setAllowBackorder(false);
        }
        if (variant.getManageInventory() == null) {
            variant.setManageInventory(true);
        }

        // Calculate variant rank if not provided
        if (variant.getVariantRank() == null) {
            Integer maxRank = variantRepository.getMaxVariantRankForProduct(productId);
            variant.setVariantRank(maxRank != null ? maxRank + 1 : 1);
        }

        return variantRepository.save(variant);
    }

    // READ operations
    public List<ProductVariant> getAllVariants() {
        return variantRepository.findAll();
    }

    public Optional<ProductVariant> getVariantById(String id) {
        return variantRepository.findById(id);
    }

    public List<ProductVariant> getVariantsByProductId(String productId) {
        return variantRepository.findByProductIdOrderByVariantRankAsc(productId);
    }

    public List<ProductVariant> searchVariantsByTitle(String title) {
        return variantRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<ProductVariant> searchVariantsBySku(String sku) {
        return variantRepository.findBySkuContainingIgnoreCase(sku);
    }

    public Optional<ProductVariant> findVariantBySku(String sku) {
        return variantRepository.findBySku(sku);
    }

    public Optional<ProductVariant> findVariantByBarcode(String barcode) {
        return variantRepository.findByBarcode(barcode);
    }

    // UPDATE operations
    public Optional<ProductVariant> updateVariant(String variantId, ProductVariant updates) {
        Optional<ProductVariant> existingVariant = variantRepository.findById(variantId);
        if (existingVariant.isEmpty()) {
            return Optional.empty();
        }

        ProductVariant variant = existingVariant.get();

        // Update fields if provided
        if (updates.getTitle() != null && !updates.getTitle().trim().isEmpty()) {
            variant.setTitle(updates.getTitle());
        }
        if (updates.getSku() != null && !updates.getSku().trim().isEmpty()) {
            variant.setSku(updates.getSku());
        }
        if (updates.getBarcode() != null && !updates.getBarcode().trim().isEmpty()) {
            variant.setBarcode(updates.getBarcode());
        }
        if (updates.getEan() != null) {
            variant.setEan(updates.getEan());
        }
        if (updates.getUpc() != null) {
            variant.setUpc(updates.getUpc());
        }
        if (updates.getAllowBackorder() != null) {
            variant.setAllowBackorder(updates.getAllowBackorder());
        }
        if (updates.getManageInventory() != null) {
            variant.setManageInventory(updates.getManageInventory());
        }
        if (updates.getHsCode() != null) {
            variant.setHsCode(updates.getHsCode());
        }
        if (updates.getOriginCountry() != null) {
            variant.setOriginCountry(updates.getOriginCountry());
        }
        if (updates.getMaterial() != null) {
            variant.setMaterial(updates.getMaterial());
        }
        if (updates.getWeight() != null) {
            variant.setWeight(updates.getWeight());
        }
        if (updates.getLength() != null) {
            variant.setLength(updates.getLength());
        }
        if (updates.getHeight() != null) {
            variant.setHeight(updates.getHeight());
        }
        if (updates.getWidth() != null) {
            variant.setWidth(updates.getWidth());
        }
        if (updates.getMetadata() != null) {
            variant.setMetadata(updates.getMetadata());
        }
        if (updates.getVariantRank() != null) {
            variant.setVariantRank(updates.getVariantRank());
        }

        variant.setUpdatedAt(LocalDateTime.now());
        return Optional.of(variantRepository.save(variant));
    }

    public Optional<ProductVariant> updateVariantBasicInfo(String variantId, String title, String sku, String barcode) {
        Optional<ProductVariant> existingVariant = variantRepository.findById(variantId);
        if (existingVariant.isEmpty()) {
            return Optional.empty();
        }

        ProductVariant variant = existingVariant.get();

        if (title != null && !title.trim().isEmpty()) {
            variant.setTitle(title);
        }
        if (sku != null && !sku.trim().isEmpty()) {
            variant.setSku(sku);
        }
        if (barcode != null && !barcode.trim().isEmpty()) {
            variant.setBarcode(barcode);
        }

        variant.setUpdatedAt(LocalDateTime.now());
        return Optional.of(variantRepository.save(variant));
    }

    // DELETE operations
    public boolean deleteVariant(String variantId) {
        if (variantRepository.existsById(variantId)) {
            variantRepository.deleteById(variantId);
            return true;
        }
        return false;
    }

    public boolean softDeleteVariant(String variantId) {
        Optional<ProductVariant> variant = variantRepository.findById(variantId);
        if (variant.isPresent()) {
            ProductVariant v = variant.get();
            v.setDeletedAt(LocalDateTime.now());
            variantRepository.save(v);
            return true;
        }
        return false;
    }

    public int deleteVariantsByProductId(String productId) {
        List<ProductVariant> variants = variantRepository.findByProductId(productId);
        variantRepository.deleteAll(variants);
        return variants.size();
    }

    // UTILITY operations
    public boolean variantExists(String variantId) {
        return variantRepository.existsById(variantId);
    }

    public boolean skuExists(String sku) {
        return variantRepository.findBySku(sku).isPresent();
    }

    public boolean barcodeExists(String barcode) {
        return variantRepository.findByBarcode(barcode).isPresent();
    }

    public long getVariantCount() {
        return variantRepository.count();
    }

    public long getVariantCountForProduct(String productId) {
        return variantRepository.countByProductId(productId);
    }

    public List<ProductVariant> getActiveVariants() {
        return variantRepository.findByDeletedAtIsNull();
    }

    public List<ProductVariant> getActiveVariantsByProductId(String productId) {
        return variantRepository.findByProductIdAndDeletedAtIsNullOrderByVariantRankAsc(productId);
    }

    // REORDERING operations
    public boolean reorderVariants(String productId, List<String> variantIds) {
        try {
            for (int i = 0; i < variantIds.size(); i++) {
                Optional<ProductVariant> variant = variantRepository.findById(variantIds.get(i));
                if (variant.isPresent() && variant.get().getProductId().equals(productId)) {
                    ProductVariant v = variant.get();
                    v.setVariantRank(i + 1);
                    v.setUpdatedAt(LocalDateTime.now());
                    variantRepository.save(v);
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // BULK operations
    public List<ProductVariant> createMultipleVariants(String productId, List<ProductVariant> variants) {
        // Validate product exists
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            throw new IllegalArgumentException("Product with ID " + productId + " not found");
        }

        Integer currentMaxRank = variantRepository.getMaxVariantRankForProduct(productId);
        int nextRank = currentMaxRank != null ? currentMaxRank + 1 : 1;

        for (ProductVariant variant : variants) {
            // Generate variant ID if not provided
            if (variant.getId() == null || variant.getId().isEmpty()) {
                String uuid = UUID.randomUUID().toString().replace("-", "");
                variant.setId("variant_" + uuid.substring(0, 8));
            }

            variant.setProductId(productId);
            variant.setCreatedAt(LocalDateTime.now());
            variant.setUpdatedAt(LocalDateTime.now());

            // Set default values if not provided
            if (variant.getAllowBackorder() == null) {
                variant.setAllowBackorder(false);
            }
            if (variant.getManageInventory() == null) {
                variant.setManageInventory(true);
            }
            if (variant.getVariantRank() == null) {
                variant.setVariantRank(nextRank++);
            }
        }

        return variantRepository.saveAll(variants);
    }

    // SEARCH and FILTER operations
    public List<ProductVariant> findVariantsByProductTitle(String productTitle) {
        return variantRepository.findVariantsByProductTitle(productTitle);
    }

    public List<ProductVariant> findVariantsWithLowStock(int threshold) {
        // This would need to be implemented based on your inventory system
        // For now, returning empty list as placeholder
        return List.of();
    }
}