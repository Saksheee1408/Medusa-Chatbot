package org.example.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "inventory_level")
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLevel {

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem;

    @Column(name = "stocked_quantity")
    private BigDecimal stockedQuantity;

    @Column(name = "reserved_quantity")
    private BigDecimal reservedQuantity;

    @Column(name = "incoming_quantity")
    private BigDecimal incomingQuantity;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = "il_" + UUID.randomUUID();
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

    public InventoryItem getInventoryItem() {
        return inventoryItem;
    }

    public void setInventoryItem(InventoryItem inventoryItem) {
        this.inventoryItem = inventoryItem;
    }
    public BigDecimal getReserved() {
        return reservedQuantity != null ? reservedQuantity : BigDecimal.ZERO;
    }
    public BigDecimal getIncoming() {
        return incomingQuantity != null ? incomingQuantity : BigDecimal.ZERO;
    }

    public BigDecimal getStockedQuantity() {
        return stockedQuantity;
    }

    public void setStockedQuantity(BigDecimal stockedQuantity) {
        this.stockedQuantity = stockedQuantity;
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(BigDecimal reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public BigDecimal getIncomingQuantity() {
        return incomingQuantity;
    }

    public void setIncomingQuantity(BigDecimal incomingQuantity) {
        this.incomingQuantity = incomingQuantity;
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
    @Transient
    public BigDecimal getAvailable() {
        if (stockedQuantity == null) stockedQuantity = BigDecimal.ZERO;
        if (reservedQuantity == null) reservedQuantity = BigDecimal.ZERO;
        if (incomingQuantity == null) incomingQuantity = BigDecimal.ZERO;
        return stockedQuantity.subtract(reservedQuantity).add(incomingQuantity);
    }




}
