package org.example.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product_category")
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategory {

    @Id
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "mpath")
    private String mpath;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_internal")
    private Boolean isInternal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private ProductCategory parentCategory;

    @OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY)
    private List<ProductCategory> subCategories;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = "cat_" + UUID.randomUUID();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMpath() {
        return mpath;
    }

    public void setMpath(String mpath) {
        this.mpath = mpath;
    }

    public Boolean getIsActive() {
        return isActive;
    }


    public void setActive(Boolean active) {
        isActive = active;
    }
    public String getParentCategoryId() {
        return parentCategory != null ? parentCategory.getId() : null;
    }


    public Boolean getInternal() {
        return isInternal;
    }

    public void setInternal(Boolean internal) {
        isInternal = internal;
    }

    public ProductCategory getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(ProductCategory parentCategory) {
        this.parentCategory = parentCategory;
    }

    public List<ProductCategory> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<ProductCategory> subCategories) {
        this.subCategories = subCategories;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
