package org.example.Service;

import org.example.Model.Product;
import org.example.Model.ProductVariant;
import org.example.Repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EnhancedChatbotService {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantService variantService;

    @Autowired
    private ProductVariantRepository variantRepository;

    public String processMessage(String message) {
        String normalizedMessage = message.toLowerCase().trim();

        try {
            // VARIANT-specific operations
            if (normalizedMessage.contains("variant")) {
                return handleVariantOperations(message);
            }

            // STOCK CHECK operations (leveraging your existing functionality)
            if (normalizedMessage.contains("stock") || normalizedMessage.contains("inventory")) {
                return handleStockQuery(message);
            }

            // CREATE operations
            if (normalizedMessage.contains("create") || normalizedMessage.contains("add")) {
                return handleCreateProduct(message);
            }

            // READ operations
            if (normalizedMessage.contains("show") || normalizedMessage.contains("get") ||
                    normalizedMessage.contains("find") || normalizedMessage.contains("list")) {
                return handleReadProduct(message);
            }

            // UPDATE operations
            if (normalizedMessage.contains("update") || normalizedMessage.contains("modify") ||
                    normalizedMessage.contains("change") || normalizedMessage.contains("edit")) {
                return handleUpdateProduct(message);
            }

            // DELETE operations
            if (normalizedMessage.contains("delete") || normalizedMessage.contains("remove")) {
                return handleDeleteProduct(message);
            }

            // HELP
            if (normalizedMessage.contains("help") || normalizedMessage.contains("commands")) {
                return getHelpMessage();
            }

            // STATUS/COUNT
            if (normalizedMessage.contains("count") || normalizedMessage.contains("total") || normalizedMessage.contains("status")) {
                return getStatusMessage();
            }

            return getHelpMessage();

        } catch (Exception e) {
            return "❌ Error processing request: " + e.getMessage();
        }
    }

    private String handleVariantOperations(String message) {
        String normalizedMessage = message.toLowerCase().trim();

        // CREATE VARIANT
        if (normalizedMessage.contains("create") || normalizedMessage.contains("add")) {
            return handleCreateVariant(message);
        }

        // SHOW/LIST VARIANTS
        if (normalizedMessage.contains("show") || normalizedMessage.contains("list") || normalizedMessage.contains("get")) {
            return handleShowVariants(message);
        }

        // UPDATE VARIANT
        if (normalizedMessage.contains("update") || normalizedMessage.contains("modify") || normalizedMessage.contains("edit")) {
            return handleUpdateVariant(message);
        }

        // DELETE VARIANT
        if (normalizedMessage.contains("delete") || normalizedMessage.contains("remove")) {
            return handleDeleteVariant(message);
        }

        return "❌ Please specify variant operation: create, show, update, or delete variant";
    }

    private String handleCreateVariant(String message) {
        try {
            String productId = extractProductId(message);
            if (productId == null) {
                return "❌ Please specify a product ID for the variant.\nExample: 'Create variant for product prod_123 with title Red Large and sku RED-L-001'";
            }

            // Check if product exists
            if (!productService.productExists(productId)) {
                return "❌ Product with ID '" + productId + "' not found.";
            }

            String title = extractValue(message, "title");
            String sku = extractValue(message, "sku");
            String barcode = extractValue(message, "barcode");

            if (title == null || title.trim().isEmpty()) {
                return "❌ Please specify a variant title.\nExample: 'Create variant for product prod_123 with title Red Large and sku RED-L-001'";
            }

            ProductVariant variant = variantService.createVariant(productId, title, sku, barcode);

            return String.format("✅ **Variant Created Successfully!**\n" +
                            "🆔 Variant ID: %s\n" +
                            "📦 Product ID: %s\n" +
                            "📝 Title: %s\n" +
                            "🏷️ SKU: %s\n" +
                            "📊 Barcode: %s\n" +
                            "📈 Rank: %d",
                    variant.getId(),
                    variant.getProductId(),
                    variant.getTitle(),
                    variant.getSku() != null ? variant.getSku() : "Not set",
                    variant.getBarcode() != null ? variant.getBarcode() : "Not set",
                    variant.getVariantRank());

        } catch (Exception e) {
            return "❌ Error creating variant: " + e.getMessage();
        }
    }

    private String handleShowVariants(String message) {
        try {
            String productId = extractProductId(message);
            String variantId = extractVariantId(message);

            // Show specific variant
            if (variantId != null) {
                Optional<ProductVariant> variant = variantService.getVariantById(variantId);
                if (variant.isPresent()) {
                    return formatVariantDetails(variant.get());
                } else {
                    return "❌ Variant with ID '" + variantId + "' not found.";
                }
            }

            // Show variants for specific product
            if (productId != null) {
                List<ProductVariant> variants = variantService.getVariantsByProductId(productId);
                if (variants.isEmpty()) {
                    return "📦 No variants found for product ID '" + productId + "'";
                }
                return formatVariantList(variants, "Variants for product " + productId);
            }

            // Show all variants
            List<ProductVariant> allVariants = variantService.getAllVariants();
            if (allVariants.isEmpty()) {
                return "📦 No variants found in the system.";
            }
            return formatVariantList(allVariants, "All Product Variants");

        } catch (Exception e) {
            return "❌ Error retrieving variants: " + e.getMessage();
        }
    }

    private String handleUpdateVariant(String message) {
        try {
            String variantId = extractVariantId(message);
            if (variantId == null) {
                return "❌ Please specify a variant ID.\nExample: 'Update variant variant_123 with title Blue Medium'";
            }

            String title = extractValue(message, "title");
            String sku = extractValue(message, "sku");
            String barcode = extractValue(message, "barcode");

            if (title == null && sku == null && barcode == null) {
                return "❌ Please specify at least one field to update (title, sku, or barcode)";
            }

            Optional<ProductVariant> updatedVariant = variantService.updateVariantBasicInfo(variantId, title, sku, barcode);

            if (updatedVariant.isPresent()) {
                return "✅ **Variant Updated Successfully!**\n" + formatVariantDetails(updatedVariant.get());
            } else {
                return "❌ Variant with ID '" + variantId + "' not found.";
            }

        } catch (Exception e) {
            return "❌ Error updating variant: " + e.getMessage();
        }
    }

    private String handleDeleteVariant(String message) {
        try {
            String variantId = extractVariantId(message);
            if (variantId == null) {
                return "❌ Please specify a variant ID.\nExample: 'Delete variant variant_123'";
            }

            // Get variant info before deletion for confirmation message
            Optional<ProductVariant> variantToDelete = variantService.getVariantById(variantId);

            boolean deleted = variantService.deleteVariant(variantId);

            if (deleted) {
                String variantTitle = variantToDelete.isPresent() ? variantToDelete.get().getTitle() : variantId;
                return "✅ **Variant Deleted Successfully!**\n" +
                        "🗑️ Variant '" + variantTitle + "' has been removed from the system.";
            } else {
                return "❌ Variant '" + variantId + "' not found.";
            }

        } catch (Exception e) {
            return "❌ Error deleting variant: " + e.getMessage();
        }
    }

    private String handleStockQuery(String message) {
        try {
            String productName = extractProductNameForStock(message);
            if (productName == null) {
                return "❌ Please specify a product name for stock check.\nExample: 'Check stock for Premium T-Shirt'";
            }

            Integer totalStock = variantRepository.getTotalStockByProductName(productName);

            return String.format("📦 **Stock Information**\n" +
                            "Product: %s\n" +
                            "Total Stock: %d units",
                    productName,
                    totalStock != null ? totalStock : 0);

        } catch (Exception e) {
            return "❌ Error checking stock: " + e.getMessage();
        }
    }

    private String handleCreateProduct(String message) {
        try {
            String title = extractValue(message, "title");
            String description = extractValue(message, "description");
            String status = extractValue(message, "status");

            if (title == null || title.isEmpty()) {
                return "❌ Please specify a product title.\nExample: 'Create product with title Premium T-Shirt and description High quality cotton shirt'";
            }

            // Check if product with same title already exists
            if (productService.productExistsByTitle(title)) {
                return "❌ A product with the title '" + title + "' already exists. Please use a different title.";
            }

            // Use the enhanced ProductService method
            Product product = new Product();
            product.setTitle(title);
            product.setDescription(description != null ? description : "");
            product.setStatus(status != null ? status : "published"); // Default to published for chatbot

            Product savedProduct = productService.createProduct(product);

            // Get variant count for display
            long variantCount = productService.getVariantCountForProduct(savedProduct.getId());

            return String.format("✅ **Product Created Successfully!**\n" +
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

        } catch (Exception e) {
            return "❌ Error creating product: " + e.getMessage();
        }
    }

    private String handleReadProduct(String message) {
        try {
            // Check for specific product ID
            String productId = extractProductId(message);
            if (productId != null) {
                Optional<Product> product = productService.getProductById(productId);
                if (product.isPresent()) {
                    return formatProductDetailsWithVariants(product.get());
                } else {
                    return "❌ Product with ID '" + productId + "' not found.";
                }
            }

            // Check for search by title
            String searchTitle = extractValue(message, "title");
            if (searchTitle == null) {
                searchTitle = extractQuotedValue(message);
            }
            if (searchTitle == null) {
                searchTitle = extractProductNameGeneral(message);
            }

            if (searchTitle != null) {
                List<Product> products = productService.searchProductsByTitle(searchTitle);
                if (products.isEmpty()) {
                    return "❌ No products found with title containing '" + searchTitle + "'";
                }
                return formatProductList(products, "Products matching '" + searchTitle + "':");
            }

            // Show all products or published products
            if (message.toLowerCase().contains("published")) {
                List<Product> products = productService.getPublishedProducts();
                return formatProductList(products, "Published Products:");
            } else if (message.toLowerCase().contains("all")) {
                List<Product> products = productService.getAllProducts();
                return formatProductList(products, "All Products:");
            } else {
                // Default to published products
                List<Product> products = productService.getPublishedProducts();
                return formatProductList(products, "Published Products:");
            }

        } catch (Exception e) {
            return "❌ Error retrieving products: " + e.getMessage();
        }
    }

    private String handleUpdateProduct(String message) {
        try {
            String productId = extractProductId(message);
            String title = extractValue(message, "title");
            String description = extractValue(message, "description");
            String status = extractValue(message, "status");

            if (productId == null) {
                return "❌ Please specify a product ID.\nExample: 'Update product prod_123 with title New Title'";
            }

            Optional<Product> updatedProduct = productService.updateProductById(productId, title, description, status);

            if (updatedProduct.isPresent()) {
                return "✅ **Product Updated Successfully!**\n" + formatProductDetailsWithVariants(updatedProduct.get());
            } else {
                return "❌ Product with ID '" + productId + "' not found.";
            }

        } catch (Exception e) {
            return "❌ Error updating product: " + e.getMessage();
        }
    }

    private String handleDeleteProduct(String message) {
        try {
            // Extract identifier (could be ID or title)
            String identifier = extractProductId(message);
            if (identifier == null) {
                identifier = extractValue(message, "title");
            }
            if (identifier == null) {
                identifier = extractQuotedValue(message);
            }
            if (identifier == null) {
                identifier = extractProductNameGeneral(message);
            }

            if (identifier == null) {
                return "❌ Please specify a product ID or title.\nExamples:\n• 'Delete product prod_123'\n• 'Delete product Premium T-Shirt'";
            }

            // Get product info before deletion for confirmation message
            Optional<Product> productToDelete = null;
            if (identifier.startsWith("prod_")) {
                productToDelete = productService.getProductById(identifier);
            } else {
                productToDelete = productService.findProductByExactTitle(identifier);
            }

            // Get variant count before deletion
            long variantCount = 0;
            if (productToDelete.isPresent()) {
                variantCount = productService.getVariantCountForProduct(productToDelete.get().getId());
            }

            // Attempt deletion using enhanced method (this will also delete variants)
            boolean deleted = productService.deleteProduct(identifier);

            if (deleted) {
                String productName = productToDelete.isPresent() ? productToDelete.get().getTitle() : identifier;
                return "✅ **Product Deleted Successfully!**\n" +
                        "🗑️ Product '" + productName + "' and its " + variantCount + " variant(s) have been removed from the system.";
            } else {
                return "❌ Product '" + identifier + "' not found. Please check the ID or title and try again.";
            }

        } catch (Exception e) {
            return "❌ Error deleting product: " + e.getMessage();
        }
    }

    private String getHelpMessage() {
        return "🤖 **Enhanced Product & Variant Management Chatbot**\n\n" +
                "📋 **Product Commands:**\n\n" +
                "**CREATE:**\n" +
                "• 'Create product with title [name] and description [desc]'\n" +
                "• 'Add product with title [name]'\n\n" +
                "**READ:**\n" +
                "• 'Show all products' or 'List all products'\n" +
                "• 'Show published products'\n" +
                "• 'Find product [name]' or 'Get product [id]'\n\n" +
                "**UPDATE:**\n" +
                "• 'Update product [id] with title [new title]'\n" +
                "• 'Update product [id] with description [new desc]'\n\n" +
                "**DELETE:**\n" +
                "• 'Delete product [id]' or 'Delete product [title]'\n" +
                "• 'Remove product [id]'\n\n" +
                "📦 **Variant Commands:**\n\n" +
                "**CREATE VARIANT:**\n" +
                "• 'Create variant for product [id] with title [name] and sku [sku]'\n" +
                "• 'Add variant for product [id] with title [name]'\n\n" +
                "**SHOW VARIANTS:**\n" +
                "• 'Show variants for product [id]'\n" +
                "• 'Get variant [variant_id]'\n" +
                "• 'List all variants'\n\n" +
                "**UPDATE VARIANT:**\n" +
                "• 'Update variant [variant_id] with title [new title]'\n" +
                "• 'Update variant [variant_id] with sku [new sku]'\n\n" +
                "**DELETE VARIANT:**\n" +
                "• 'Delete variant [variant_id]'\n" +
                "• 'Remove variant [variant_id]'\n\n" +
                "**STOCK:**\n" +
                "• 'Check stock for [product name]'\n\n" +
                "**OTHER:**\n" +
                "• 'Status' or 'Count' - Get product and variant statistics\n" +
                "• 'Help' - Show this message";
    }

    private String getStatusMessage() {
        try {
            long totalProducts = productService.getProductCount();
            long publishedProducts = productService.getPublishedProductCount();
            long draftProducts = totalProducts - publishedProducts;
            long totalVariants = productService.getVariantCount();

            return String.format("📊 **System Statistics**\n\n" +
                            "📦 Total Products: %d\n" +
                            "✅ Published: %d\n" +
                            "📝 Draft: %d\n" +
                            "🔄 Total Variants: %d\n" +
                            "📈 Avg Variants per Product: %.1f",
                    totalProducts, publishedProducts, draftProducts, totalVariants,
                    totalProducts > 0 ? (double) totalVariants / totalProducts : 0.0);
        } catch (Exception e) {
            return "❌ Error retrieving statistics: " + e.getMessage();
        }
    }

    // Helper methods for variant operations
    private String extractVariantId(String message) {
        // Look for variant ID patterns (variant_ prefix)
        Pattern pattern = Pattern.compile("variant\\s+(variant_[a-zA-Z0-9_-]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // Look for just the variant_ pattern
        pattern = Pattern.compile("(variant_[a-zA-Z0-9_-]+)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String formatVariantDetails(ProductVariant variant) {
        return String.format("🔄 **Variant Details**\n\n" +
                        "🆔 Variant ID: %s\n" +
                        "📦 Product ID: %s\n" +
                        "📝 Title: %s\n" +
                        "🏷️ SKU: %s\n" +
                        "📊 Barcode: %s\n" +
                        "📈 Rank: %d\n" +
                        "⚙️ Manage Inventory: %s\n" +
                        "🔄 Allow Backorder: %s\n" +
                        "📏 Dimensions: %s",
                variant.getId(),
                variant.getProductId(),
                variant.getTitle(),
                variant.getSku() != null ? variant.getSku() : "Not set",
                variant.getBarcode() != null ? variant.getBarcode() : "Not set",
                variant.getVariantRank(),
                variant.getManageInventory() != null ? variant.getManageInventory() : "Not set",
                variant.getAllowBackorder() != null ? variant.getAllowBackorder() : "Not set",
                formatDimensions(variant));
    }

    private String formatVariantList(List<ProductVariant> variants, String header) {
        if (variants.isEmpty()) {
            return "❌ No variants found.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🔄 **").append(header).append("**\n\n");

        for (int i = 0; i < Math.min(variants.size(), 10); i++) { // Limit to 10 variants
            ProductVariant variant = variants.get(i);
            sb.append(String.format("%d. **%s** (ID: `%s`)\n",
                    i + 1,
                    variant.getTitle(),
                    variant.getId()));
            sb.append(String.format("   📦 Product ID: %s | 🏷️ SKU: %s\n",
                    variant.getProductId(),
                    variant.getSku() != null ? variant.getSku() : "Not set"));
            sb.append(String.format("   📈 Rank: %d | 📊 Barcode: %s\n",
                    variant.getVariantRank(),
                    variant.getBarcode() != null ? variant.getBarcode() : "Not set"));
            sb.append("\n");
        }

        if (variants.size() > 10) {
            sb.append(String.format("... and %d more variants\n", variants.size() - 10));
        }

        return sb.toString();
    }

    private String formatProductDetailsWithVariants(Product product) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📦 **Product Details**\n\n" +
                        "🆔 ID: %s\n" +
                        "📝 Title: %s\n" +
                        "🔗 Handle: %s\n" +
                        "📊 Status: %s\n" +
                        "📄 Description: %s\n",
                product.getId(),
                product.getTitle(),
                product.getHandle() != null ? product.getHandle() : "Not set",
                product.getStatus(),
                product.getDescription() != null && !product.getDescription().isEmpty() ?
                        product.getDescription() : "No description"));

        // Add variant information
        List<ProductVariant> variants = productService.getProductVariants(product.getId());
        sb.append(String.format("🔢 Variants: %d\n", variants.size()));

        if (!variants.isEmpty()) {
            sb.append("\n**Variants:**\n");
            for (int i = 0; i < Math.min(variants.size(), 5); i++) {
                ProductVariant variant = variants.get(i);
                sb.append(String.format("  %d. %s (ID: %s, SKU: %s)\n",
                        i + 1,
                        variant.getTitle(),
                        variant.getId(),
                        variant.getSku() != null ? variant.getSku() : "No SKU"));
            }
            if (variants.size() > 5) {
                sb.append(String.format("  ... and %d more variants\n", variants.size() - 5));
            }
        }

        return sb.toString();
    }

    private String formatDimensions(ProductVariant variant) {
        StringBuilder dims = new StringBuilder();
        if (variant.getWeight() != null) dims.append("Weight: ").append(variant.getWeight()).append("g ");
        if (variant.getLength() != null) dims.append("Length: ").append(variant.getLength()).append("cm ");
        if (variant.getHeight() != null) dims.append("Height: ").append(variant.getHeight()).append("cm ");
        if (variant.getWidth() != null) dims.append("Width: ").append(variant.getWidth()).append("cm");

        return dims.length() > 0 ? dims.toString().trim() : "Not set";
    }

    // Existing helper methods (keeping the same extraction logic)
    private String extractProductNameForStock(String message) {
        Pattern pattern = Pattern.compile("(?:stock|inventory)\\s+(?:for|of)\\s+([^\\n\\r]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        pattern = Pattern.compile("(?:stock|inventory)\\s+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String extractValue(String message, String key) {
        Pattern pattern = Pattern.compile(key + "\\s+([^\\s][^,\\n\\r]*?)(?=\\s+(?:and|with|description|title|status|handle|sku|barcode)|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        pattern = Pattern.compile(key + "\\s+['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String extractQuotedValue(String message) {
        Pattern pattern = Pattern.compile("['\"]([^'\"]+)['\"]");
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractProductId(String message) {
        Pattern pattern = Pattern.compile("product\\s+(prod_[a-zA-Z0-9_-]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }

        pattern = Pattern.compile("(prod_[a-zA-Z0-9_-]+)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }

        pattern = Pattern.compile("id\\s+([a-zA-Z0-9_-]+)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String extractProductNameGeneral(String message) {
        Pattern pattern = Pattern.compile("(?:delete|remove|find|get)\\s+product\\s+([^\\n\\r]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String extracted = matcher.group(1).trim();
            if (!extracted.startsWith("prod_")) {
                return extracted;
            }
        }
        return null;
    }

    private String formatProductList(List<Product> products, String header) {
        if (products.isEmpty()) {
            return "❌ No products found.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("📋 **").append(header).append("**\n\n");

        for (int i = 0; i < Math.min(products.size(), 10); i++) {
            Product product = products.get(i);
            long variantCount = productService.getVariantCountForProduct(product.getId());

            sb.append(String.format("%d. **%s** (ID: `%s`)\n",
                    i + 1,
                    product.getTitle(),
                    product.getId()));
            sb.append(String.format("   📊 Status: %s | 🔗 Handle: %s | 🔄 Variants: %d\n",
                    product.getStatus(),
                    product.getHandle() != null ? product.getHandle() : "Not set",
                    variantCount));
            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                sb.append(String.format("   📄 Description: %s\n",
                        product.getDescription().length() > 50 ?
                                product.getDescription().substring(0, 50) + "..." :
                                product.getDescription()));
            }
            sb.append("\n");
        }

        if (products.size() > 10) {
            sb.append(String.format("... and %d more products\n", products.size() - 10));
        }

        return sb.toString();
    }
}