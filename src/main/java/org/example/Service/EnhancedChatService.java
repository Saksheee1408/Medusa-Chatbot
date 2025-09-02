package org.example.Service;

import org.example.Model.*;
import org.example.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class EnhancedChatService {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private ProductVariantRepository variantRepo;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private PricingService pricingService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String apiKey;

    public String chatWithGemini(String message) {
        // Build comprehensive context with all relevant data
        StringBuilder context = buildComprehensiveContext();

        String prompt = createEnhancedPrompt(context.toString(), message);
        String requestBody = createGeminiRequest(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return extractGeminiResponse(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I couldn't process your request at the moment. Error: " + e.getMessage();
        }
    }

    private StringBuilder buildComprehensiveContext() {
        StringBuilder context = new StringBuilder();
        context.append("=== COMPREHENSIVE E-COMMERCE INVENTORY SYSTEM ===\n\n");

        // 1. Products with full details
        context.append("PRODUCTS:\n");
        List<Product> products = productRepo.findPublishedProducts();
        for (Product product : products) {
            context.append("Product ID: ").append(product.getId()).append("\n");
            context.append("Title: ").append(product.getTitle()).append("\n");
            context.append("Handle: ").append(product.getHandle()).append("\n");
            context.append("Status: ").append(product.getStatus()).append("\n");
            if (product.getDescription() != null) {
                context.append("Description: ").append(product.getDescription()).append("\n");
            }

            // Add variant information
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                context.append("Variants:\n");
                for (ProductVariant variant : product.getVariants()) {
                    context.append("  - Variant ID: ").append(variant.getId()).append("\n");
                    context.append("    Title: ").append(variant.getTitle()).append("\n");
                    context.append("    SKU: ").append(variant.getSku()).append("\n");

                    // Add stock information for this variant
                    addStockInfoForVariant(context, variant.getId());

                    // Add pricing information for this variant
                    addPricingInfoForVariant(context, variant.getId());
                }
            }

            // Add product-level pricing
            addPricingInfoForProduct(context, product.getId());

            context.append("---\n");
        }

        // 2. Categories
        context.append("\nCATEGORIES:\n");
        List<ProductCategory> categories = categoryService.getAllActiveCategories();
        for (ProductCategory category : categories) {
            context.append("Category ID: ").append(category.getId()).append("\n");
            context.append("Name: ").append(category.getName()).append("\n");
            if (category.getDescription() != null) {
                context.append("Description: ").append(category.getDescription()).append("\n");
            }
            context.append("Active: ").append(category.getIsActive()).append("\n");
            if (category.getParentCategoryId() != null) {
                context.append("Parent Category ID: ").append(category.getParentCategoryId()).append("\n");
            }
            context.append("---\n");
        }

        // 3. Inventory Items (standalone items not tied to variants)
        context.append("\nINVENTORY ITEMS:\n");
        List<InventoryItem> inventoryItems = inventoryService.getAllActiveItems();
        for (InventoryItem item : inventoryItems) {
            context.append("Inventory Item ID: ").append(item.getId()).append("\n");
            context.append("Title: ").append(item.getTitle()).append("\n");
            context.append("SKU: ").append(item.getSku()).append("\n");
            if (item.getDescription() != null) {
                context.append("Description: ").append(item.getDescription()).append("\n");
            }

            // Add dimensions if available
            if (item.getWeight() != null || item.getLength() != null ||
                    item.getHeight() != null || item.getWidth() != null) {
                context.append("Dimensions: ");
                if (item.getWeight() != null) context.append("Weight: ").append(item.getWeight()).append("g ");
                if (item.getLength() != null) context.append("Length: ").append(item.getLength()).append("cm ");
                if (item.getHeight() != null) context.append("Height: ").append(item.getHeight()).append("cm ");
                if (item.getWidth() != null) context.append("Width: ").append(item.getWidth()).append("cm");
                context.append("\n");
            }

            // Add stock levels
            Optional<InventoryLevel> stockLevel = inventoryService.getStockLevel(item.getId());
            if (stockLevel.isPresent()) {
                InventoryLevel level = stockLevel.get();
                context.append("Stock - Available: ").append(level.getAvailable())
                        .append(", Incoming: ").append(level.getIncoming())
                        .append(", Reserved: ").append(level.getReserved()).append("\n");
            }
            context.append("---\n");
        }

        // 4. Price Lists
        context.append("\nPRICE LISTS:\n");
        List<PriceList> priceLists = pricingService.getActivePriceLists();
        for (PriceList priceList : priceLists) {
            context.append("Price List ID: ").append(priceList.getId()).append("\n");
            context.append("Title: ").append(priceList.getTitle()).append("\n");
            context.append("Type: ").append(priceList.getType()).append("\n");
            context.append("Status: ").append(priceList.getStatus()).append("\n");
            if (priceList.getStartsAt() != null) {
                context.append("Starts At: ").append(priceList.getStartsAt()).append("\n");
            }
            if (priceList.getEndsAt() != null) {
                context.append("Ends At: ").append(priceList.getEndsAt()).append("\n");
            }
            context.append("---\n");
        }

        // 5. Low Stock Alert Summary
        List<InventoryLevel> lowStock = inventoryService.getLowStockItems(BigDecimal.valueOf(10));

        if (!lowStock.isEmpty()) {
            context.append("\nLOW STOCK ALERTS (Below 10 units):\n");
            for (InventoryLevel level : lowStock) {
                if (level.getInventoryItem() != null) {
                    context.append("- ").append(level.getInventoryItem().getTitle())
                            .append(" (").append(level.getAvailable()).append(" units)\n");
                }
            }
        }

        return context;
    }

    private void addStockInfoForVariant(StringBuilder context, String variantId) {
        try {
            // This assumes you have a way to map variant ID to inventory item
            // You might need to adjust this based on your actual mapping
            Optional<InventoryLevel> stockLevel = inventoryService.getStockLevel(variantId);
            if (stockLevel.isPresent()) {
                InventoryLevel level = stockLevel.get();
                context.append("    Stock - Available: ").append(level.getAvailable())
                        .append(", Incoming: ").append(level.getIncoming())
                        .append(", Reserved: ").append(level.getReserved()).append("\n");
            }
        } catch (Exception e) {
            // Handle cases where stock info is not available
        }
    }

    private void addPricingInfoForVariant(StringBuilder context, String variantId) {
        try {
            List<Price> prices = pricingService.getVariantPrices(variantId);
            if (!prices.isEmpty()) {
                context.append("    Prices:\n");
                for (Price price : prices) {
                    context.append("      - $").append(price.getAmount());
                    if (price.getPriceList() != null) {
                        context.append(" (").append(price.getPriceList().getTitle()).append(")");
                    }
                    context.append("\n");
                }
            }
        } catch (Exception e) {
            // Handle cases where pricing info is not available
        }
    }

    private void addPricingInfoForProduct(StringBuilder context, String productId) {
        try {
            List<Price> prices = pricingService.getProductPrices(productId);
            if (!prices.isEmpty()) {
                context.append("Product Prices:\n");
                for (Price price : prices) {
                    context.append("  - $").append(price.getAmount());
                    if (price.getPriceList() != null) {
                        context.append(" (").append(price.getPriceList().getTitle()).append(")");
                    }
                    context.append("\n");
                }
            }
        } catch (Exception e) {
            // Handle cases where pricing info is not available
        }
    }

    private String createEnhancedPrompt(String context, String message) {
        return "You are an advanced e-commerce assistant with access to a comprehensive inventory management system.\n\n" +
                "SYSTEM CAPABILITIES:\n" +
                "- Product catalog with variants and detailed information\n" +
                "- Real-time inventory tracking with stock levels\n" +
                "- Dynamic pricing with multiple price lists\n" +
                "- Category hierarchy management\n" +
                "- Low stock monitoring and alerts\n\n" +
                "CURRENT SYSTEM DATA:\n" + context + "\n\n" +
                "INSTRUCTIONS:\n" +
                "- Provide specific, accurate information based on the data above\n" +
                "- When discussing stock, always mention current availability\n" +
                "- For pricing queries, show all relevant price options\n" +
                "- Include product variants when relevant\n" +
                "- Suggest related products or categories when appropriate\n" +
                "- Alert users about low stock items when relevant\n" +
                "- Use clear formatting with emojis for better readability\n" +
                "- If data is missing, acknowledge it and suggest alternatives\n\n" +
                "USER QUERY: " + message + "\n\n" +
                "RESPONSE:";
    }

    private String createGeminiRequest(String prompt) {
        String escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{\n" +
                "  \"contents\": [{\n" +
                "    \"parts\": [{\n" +
                "      \"text\": \"" + escapedPrompt + "\"\n" +
                "    }]\n" +
                "  }]\n" +
                "}";
    }

    private String extractGeminiResponse(String rawResponse) {
        try {
            if (rawResponse.contains("\"candidates\"")) {
                int candidatesStart = rawResponse.indexOf("\"candidates\"");
                int textStart = rawResponse.indexOf("\"text\":", candidatesStart);

                if (textStart != -1) {
                    textStart += 7;
                    while (textStart < rawResponse.length() && rawResponse.charAt(textStart) != '"') {
                        textStart++;
                    }
                    textStart++;

                    int textEnd = textStart;
                    while (textEnd < rawResponse.length()) {
                        if (rawResponse.charAt(textEnd) == '"' &&
                                (textEnd == 0 || rawResponse.charAt(textEnd - 1) != '\\')) {
                            break;
                        }
                        textEnd++;
                    }

                    if (textEnd > textStart) {
                        String extractedText = rawResponse.substring(textStart, textEnd);
                        return extractedText
                                .replace("\\n", "\n")
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\")
                                .replace("\\r", "\r")
                                .replace("\\t", "\t");
                    }
                }
            }

            return "I received your question but had trouble processing the response. Please try asking again.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error parsing response: " + e.getMessage();
        }
    }
}