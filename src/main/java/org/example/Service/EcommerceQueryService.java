package org.example.Service;

import org.example.Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class EcommerceQueryService {

    @Autowired
    private ProductService productService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PricingService pricingService;


    public Map<String, Object> getCompleteProductInfo(String productId) {
        Map<String, Object> info = new HashMap<>();

        Optional<Product> product = productService.getProductById(productId);
        if (product.isPresent()) {
            info.put("product", product.get());


            List<Price> prices = pricingService.getProductPrices(productId);
            info.put("prices", prices);


            if (product.get().getVariants() != null) {
                Map<String, Object> stockInfo = new HashMap<>();
                for (ProductVariant variant : product.get().getVariants()) {

                    Optional<InventoryLevel> stock = inventoryService.getStockLevel(variant.getId());
                    stock.ifPresent(level -> stockInfo.put(variant.getId(), level.getAvailable()));
                }
                info.put("stock", stockInfo);
            }
        }

        return info;
    }

    public List<InventoryLevel> getLowStockAlert(BigDecimal threshold) {
        return inventoryService.getLowStockItems(threshold != null ? threshold : BigDecimal.TEN);
    }


    public List<ProductCategory> getCategoryHierarchy(String categoryName) {
        Optional<ProductCategory> category = categoryService.getCategoryByName(categoryName);
        if (category.isPresent()) {
            return categoryService.getSubCategories(category.get().getId());
        }
        return List.of();
    }

    public Map<String, Object> getPriceAnalysis(String productId) {
        Map<String, Object> analysis = new HashMap<>();

        List<Price> prices = pricingService.getProductPrices(productId);
        if (!prices.isEmpty()) {
            Optional<BigDecimal> min = pricingService.getLowestPrice(productId, null);
            Optional<BigDecimal> max = pricingService.getHighestPrice(productId, null);

            analysis.put("minPrice", min.orElse(BigDecimal.ZERO));
            analysis.put("maxPrice", max.orElse(BigDecimal.ZERO));
            analysis.put("priceCount", prices.size());
            analysis.put("prices", prices);
        }

        return analysis;
    }
}