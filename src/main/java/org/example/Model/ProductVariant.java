package org.example.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "product_variant")
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    @Id
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "sku")
    private String sku;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "ean")
    private String ean;

    @Column(name = "upc")
    private String upc;

    @Column(name = "allow_backorder")
    private Boolean allowBackorder;

    @Column(name = "manage_inventory")
    private Boolean manageInventory;

    @Column(name = "hs_code")
    private String hsCode;

    @Column(name = "origin_country")
    private String originCountry;

    @Column(name = "mid_code")
    private String midCode;

    @Column(name = "material")
    private String material;

    @Column(name = "weight")
    private Integer weight;

    @Column(name = "length")
    private Integer length;

    @Column(name = "height")
    private Integer height;

    @Column(name = "width")
    private Integer width;

    // FIXED: Proper JSONB mapping
    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;

    @Column(name = "variant_rank")
    private Integer variantRank;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getEan() { return ean; }
    public void setEan(String ean) { this.ean = ean; }

    public String getUpc() { return upc; }
    public void setUpc(String upc) { this.upc = upc; }

    public Boolean getAllowBackorder() { return allowBackorder; }
    public void setAllowBackorder(Boolean allowBackorder) { this.allowBackorder = allowBackorder; }

    public Boolean getManageInventory() { return manageInventory; }
    public void setManageInventory(Boolean manageInventory) { this.manageInventory = manageInventory; }

    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }

    public String getOriginCountry() { return originCountry; }
    public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }

    public String getMidCode() { return midCode; }
    public void setMidCode(String midCode) { this.midCode = midCode; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public Integer getLength() { return length; }
    public void setLength(Integer length) { this.length = length; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    // UPDATED: Metadata getter/setter for Map<String, Object>
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public Integer getVariantRank() { return variantRank; }
    public void setVariantRank(Integer variantRank) { this.variantRank = variantRank; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    // Helper method for backward compatibility with your existing ChatService
    public Integer getInventoryQuantity() {
        // Since there's no inventory_quantity column, return null or 0
        // You'll need to implement proper inventory lookup later
        return 0;
    }
}