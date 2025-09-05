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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class EnhancedChatService {

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private ProductVariantRepository variantRepo;

    @Autowired
    private ProductService productService; // Add this dependency

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

        if (isProductCreationRequest(message)) {
            return handleProductCreationWithAutoGen(message);
        }

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

    // NEW METHOD: Check if this is a product creation request
    private boolean isProductCreationRequest(String message) {
        String normalized = message.toLowerCase().trim();
        return (normalized.contains("create") || normalized.contains("add")) &&
                normalized.contains("product") &&
                normalized.contains("title");
    }

    // NEW METHOD: Handle product creation with auto-generation
    private String handleProductCreationWithAutoGen(String message) {
        try {
            String title = extractProductTitle(message);
            String description = extractProductDescription(message);
            String status = extractProductStatus(message);

            if (title == null || title.isEmpty()) {
                return "❌ Please specify a product title.\nExample: 'Create product with title Premium T-Shirt'";
            }

            // Check if product with same title already exists
            if (productService.productExistsByTitle(title)) {
                return "❌ A product with the title '" + title + "' already exists. Please use a different title.";
            }

            // AUTO-GENERATE DESCRIPTION if not provided
            boolean descriptionGenerated = false;
            if (description == null || description.trim().isEmpty()) {
                description = generateProductDescription(title);
                descriptionGenerated = true;
            }

            // Create the product
            Product product = new Product();
            product.setTitle(title);
            product.setDescription(description);
            product.setStatus(status != null ? status : "published");

            Product savedProduct = productService.createProduct(product);
            long variantCount = productService.getVariantCountForProduct(savedProduct.getId());

            // Format response with auto-generation indicator
            String response = String.format("✅ **Product Created Successfully!**\n" +
                            "📦 ID: %s\n" +
                            "📝 Title: %s\n" +
                            "🔗 Handle: %s\n" +
                            "📊 Status: %s\n" +
                            "📄 Description: %s\n" +
                            "🔄 Variants: %d",
                    savedProduct.getId(),
                    savedProduct.getTitle(),
                    savedProduct.getHandle(),
                    savedProduct.getStatus(),
                    savedProduct.getDescription(),
                    variantCount);

            if (descriptionGenerated) {
                response += "\n🤖 Description was auto-generated using AI";
            }

            return response;

        } catch (Exception e) {
            return "❌ Error creating product: " + e.getMessage();
        }
    }

    // NEW METHOD: Generate product description using Gemini AI
    private String generateProductDescription(String productTitle) {
        String prompt = createDescriptionGenerationPrompt(productTitle);
        String requestBody = createGeminiRequest(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String generatedDescription = extractGeminiResponse(response.getBody());
            return cleanupGeneratedDescription(generatedDescription);
        } catch (Exception e) {
            // Fallback to template-based description
            return generateFallbackDescription(productTitle);
        }
    }

    // NEW METHOD: Create specialized prompt for description generation
    private String createDescriptionGenerationPrompt(String productTitle) {
        return "You are a professional e-commerce product description writer. " +
                "Create a compelling, SEO-friendly product description for: \"" + productTitle + "\"\n\n" +

                "REQUIREMENTS:\n" +
                "- Length: 2-3 sentences (50-120 words)\n" +
                "- Focus on benefits and key features\n" +
                "- Include relevant keywords naturally\n" +
                "- Professional but engaging tone\n" +
                "- No pricing information\n" +
                "- Make it suitable for e-commerce\n" +
                "- Consider target audience and use cases\n\n" +

                "ANALYSIS GUIDELINES:\n" +
                "- Identify product category from the title\n" +
                "- Determine likely materials, features, or benefits\n" +
                "- Consider common customer needs and pain points\n" +
                "- Think about what makes this product appealing\n\n" +

                "OUTPUT FORMAT:\n" +
                "Return ONLY the product description text, no quotes, no extra formatting.\n\n" +

                "EXAMPLES:\n" +
                "Title: 'Premium Cotton T-Shirt'\n" +
                "Description: Experience ultimate comfort with our premium cotton t-shirt, crafted from 100% organic cotton for exceptional breathability and long-lasting durability. This versatile wardrobe essential features a relaxed fit perfect for casual wear, layering, or everyday activities, ensuring you stay comfortable and stylish throughout your day.\n\n" +

                "Title: 'Wireless Bluetooth Headphones'\n" +
                "Description: Immerse yourself in crystal-clear audio with these advanced wireless Bluetooth headphones featuring active noise-canceling technology and an impressive 30-hour battery life. Engineered for all-day comfort with premium padding and lightweight design, these headphones deliver exceptional sound quality for music, calls, gaming, and entertainment on the go.\n\n" +

                "Title: 'Stainless Steel Water Bottle'\n" +
                "Description: Stay hydrated in style with this premium stainless steel water bottle designed to keep beverages cold for 24 hours or hot for 12 hours. Featuring a leak-proof cap, ergonomic design, and durable construction, this eco-friendly bottle is perfect for workouts, outdoor adventures, office use, and daily hydration needs.\n\n" +

                "Now generate a description for: \"" + productTitle + "\"";
    }

    // NEW METHOD: Clean up and validate generated description
    private String cleanupGeneratedDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "High-quality product designed to meet your needs with attention to detail and functionality.";
        }

        // Remove quotes and extra formatting
        description = description.replaceAll("^[\"'`]|[\"'`]$", "").trim();
        description = description.replaceAll("^Description:\\s*", "").trim();

        // Ensure reasonable length (40-250 characters)
        if (description.length() > 250) {
            // Find a good break point
            int lastPeriod = description.lastIndexOf('.', 250);
            if (lastPeriod > 150) {
                description = description.substring(0, lastPeriod + 1);
            } else {
                description = description.substring(0, 247) + "...";
            }
        }

        if (description.length() < 40) {
            description += " Carefully crafted with quality materials and attention to detail for optimal performance.";
        }

        // Ensure it starts with capital letter and ends with period
        if (!description.isEmpty()) {
            description = Character.toUpperCase(description.charAt(0)) + description.substring(1);
            if (!description.endsWith(".") && !description.endsWith("!") && !description.endsWith("?")) {
                description += ".";
            }
        }

        return description;
    }


    private String generateFallbackDescription(String productTitle) {
        String title = productTitle.toLowerCase().trim();

        // Category-based templates
        if (title.contains("shirt") || title.contains("tee") || title.contains("top")) {
            return "Comfortable and stylish " + productTitle + " crafted from quality materials for everyday wear. Perfect for casual occasions, layering, or as a versatile wardrobe essential that combines comfort with contemporary style.";

        } else if (title.contains("phone") || title.contains("mobile") || title.contains("smartphone")) {
            return "Advanced " + productTitle + " featuring cutting-edge technology, intuitive interface, and reliable performance. Designed to enhance your daily productivity and connectivity with premium build quality and user-friendly features.";

        } else if (title.contains("headphone") || title.contains("earphone") || title.contains("earbuds")) {
            return "Premium " + productTitle + " delivering exceptional audio quality with comfortable design for extended use. Features advanced sound technology, reliable connectivity, and durable construction for music, calls, and entertainment.";

        } else if (title.contains("book") || title.contains("novel") || title.contains("guide")) {
            return "Engaging " + productTitle + " offering valuable insights and knowledge for readers seeking to expand their understanding. Written with clarity and expertise to provide practical information and enjoyable reading experience.";

        } else if (title.contains("bottle") || title.contains("cup") || title.contains("mug")) {
            return "Durable " + productTitle + " designed for daily use with premium materials and functional design. Perfect for beverages at home, office, or on-the-go, combining practicality with style for your hydration needs.";

        } else if (title.contains("bag") || title.contains("backpack") || title.contains("purse")) {
            return "Versatile " + productTitle + " combining style and functionality for everyday carrying needs. Features quality construction, practical organization, and comfortable design suitable for work, travel, or casual use.";

        } else {
            return "Premium " + productTitle + " designed with attention to quality, functionality, and user satisfaction. This carefully crafted product combines innovative features with reliable performance to meet your specific needs and preferences.";
        }
    }


    private String extractProductTitle(String message) {
        // Pattern for "title [value]"
        Pattern pattern = Pattern.compile("title\\s+([^\\n\\r,]+?)(?=\\s+(?:and|with|description|status)|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }


        pattern = Pattern.compile("title\\s+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Pattern for "create product [title]" without "with title"
        pattern = Pattern.compile("(?:create|add)\\s+product\\s+([^\\n\\r]+?)(?=\\s+with|$)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            String extracted = matcher.group(1).trim();
            if (!extracted.toLowerCase().contains("with") && !extracted.toLowerCase().contains("and")) {
                return extracted;
            }
        }

        return null;
    }


    private String extractProductDescription(String message) {
        Pattern pattern = Pattern.compile("description\\s+([^\\n\\r]+?)(?=\\s+(?:and|with|title|status)|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        pattern = Pattern.compile("description\\s+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }


    private String extractProductStatus(String message) {
        Pattern pattern = Pattern.compile("status\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    // EXISTING METHODS (unchanged)
    private StringBuilder buildComprehensiveContext() {
        StringBuilder context = new StringBuilder();
        context.append("=== COMPREHENSIVE E-COMMERCE INVENTORY SYSTEM ===\n\n");

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

            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                context.append("Variants:\n");
                for (ProductVariant variant : product.getVariants()) {
                    context.append("  - Variant ID: ").append(variant.getId()).append("\n");
                    context.append("    Title: ").append(variant.getTitle()).append("\n");
                    context.append("    SKU: ").append(variant.getSku()).append("\n");

                    addStockInfoForVariant(context, variant.getId());
                    addPricingInfoForVariant(context, variant.getId());
                }
            }

            addPricingInfoForProduct(context, product.getId());
            context.append("---\n");
        }

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

        context.append("\nINVENTORY ITEMS:\n");
        List<InventoryItem> inventoryItems = inventoryService.getAllActiveItems();
        for (InventoryItem item : inventoryItems) {
            context.append("Inventory Item ID: ").append(item.getId()).append("\n");
            context.append("Title: ").append(item.getTitle()).append("\n");
            context.append("SKU: ").append(item.getSku()).append("\n");
            if (item.getDescription() != null) {
                context.append("Description: ").append(item.getDescription()).append("\n");
            }

            if (item.getWeight() != null || item.getLength() != null ||
                    item.getHeight() != null || item.getWidth() != null) {
                context.append("Dimensions: ");
                if (item.getWeight() != null) context.append("Weight: ").append(item.getWeight()).append("g ");
                if (item.getLength() != null) context.append("Length: ").append(item.getLength()).append("cm ");
                if (item.getHeight() != null) context.append("Height: ").append(item.getHeight()).append("cm ");
                if (item.getWidth() != null) context.append("Width: ").append(item.getWidth()).append("cm");
                context.append("\n");
            }

            Optional<InventoryLevel> stockLevel = inventoryService.getStockLevel(item.getId());
            if (stockLevel.isPresent()) {
                InventoryLevel level = stockLevel.get();
                context.append("Stock - Available: ").append(level.getAvailable())
                        .append(", Incoming: ").append(level.getIncoming())
                        .append(", Reserved: ").append(level.getReserved()).append("\n");
            }
            context.append("---\n");
        }

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
            Optional<InventoryLevel> stockLevel = inventoryService.getStockLevel(variantId);
            if (stockLevel.isPresent()) {
                InventoryLevel level = stockLevel.get();
                context.append("    Stock - Available: ").append(level.getAvailable())
                        .append(", Incoming: ").append(level.getIncoming())
                        .append(", Reserved: ").append(level.getReserved()).append("\n");
            }
        } catch (Exception e) {
            // Silent fail
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
            // Silent fail
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

        }
    }

    private String createEnhancedPrompt(String context, String message) {
        return "You are an advanced e-commerce assistant with access to a comprehensive inventory management system.\n\n" +
                "SYSTEM CAPABILITIES:\n" +
                "- Product catalog with variants and detailed information\n" +
                "- Real-time inventory tracking with stock levels\n" +
                "- Dynamic pricing with multiple price lists\n" +
                "- Category hierarchy management\n" +
                "- Low stock monitoring and alerts\n" +
                "- AI-powered product description generation\n\n" +
                "CURRENT SYSTEM DATA:\n" + context + "\n\n" +
                "INSTRUCTIONS:\n" +
                "- Provide specific, accurate information based on the data above\n" +
                "- When discussing stock, always mention current availability\n" +
                "- For pricing queries, show all relevant price options\n" +
                "- Include product variants when relevant\n" +
                "- Suggest related products or categories when appropriate\n" +
                "- Alert users about low stock items when relevant\n" +
                "- Use clear formatting with emojis for better readability\n" +
                "- If data is missing, acknowledge it and suggest alternatives\n" +
                "- For product creation requests, generate helpful descriptions automatically\n\n" +
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