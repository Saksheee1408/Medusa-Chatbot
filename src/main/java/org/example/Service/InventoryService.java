package org.example.Service;

import org.example.Model.InventoryItem;
import org.example.Model.InventoryLevel;
import org.example.Repository.InventoryItemRepository;
import org.example.Repository.InventoryLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryLevelRepository inventoryLevelRepository;


    public List<InventoryItem> getAllActiveItems() {
        return inventoryItemRepository.findByDeletedAtIsNull();
    }

    public Optional<InventoryItem> getItemById(String id) {
        return inventoryItemRepository.findById(id);
    }

    public List<InventoryItem> searchItemsByTitle(String title) {
        return inventoryItemRepository.findByTitleContainingIgnoreCase(title);
    }

    public Optional<InventoryItem> getItemBySku(String sku) {
        return inventoryItemRepository.findActiveBySku(sku);
    }


    public BigDecimal getTotalStockForProduct(String productName) {
        return inventoryLevelRepository.getTotalAvailableStockByProductName(productName);
    }

    public List<InventoryLevel> getLowStockItems(BigDecimal threshold) {
        return inventoryLevelRepository.findLowStockItems(threshold);
    }

    public Optional<InventoryLevel> getStockLevel(String inventoryItemId) {
        return inventoryLevelRepository.findActiveByInventoryItemId(inventoryItemId);
    }

    public List<InventoryItem> getItemsWithStock(BigDecimal minStock) {
        return inventoryItemRepository.findItemsWithStockGreaterThan(minStock);
    }

}
