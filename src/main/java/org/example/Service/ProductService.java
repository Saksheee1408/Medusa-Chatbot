package org.example.Service;

import org.example.Model.Product;
import org.example.Model.ProductVariant;
import org.example.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantService variantService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String apiKey;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(String id) {
        return productRepository.findById(id);
    }

    public List<Product> searchProductsByTitle(String title) {
        return productRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Product> getPublishedProducts() {
        return productRepository.findPublishedProducts();
    }

    // Enhanced create method with auto ID generation and AI description
    public Product createProduct(Product product) {
        if (product.getId() == null || product.getId().isEmpty()) {
            // Generate custom product ID
            String uuid = UUID.randomUUID().toString().replace("-", "");
            product.setId("prod_" + uuid.substring(0, 8));
        }

        // Generate handle from title if not provided
        if (product.getHandle() == null && product.getTitle() != null) {
            product.setHandle(generateHandle(product.getTitle()));
        }

        // Set default status if not provided
        if (product.getStatus() == null) {
            product.setStatus("draft");
        }

        // AUTO-GENERATE DESCRIPTION using Gemini AI
        if ((product.getDescription() == null || product.getDescription().trim().isEmpty())
                && product.getTitle() != null) {
            try {
                System.out.println("🤖 Generating AI description for: " + product.getTitle());
                String aiDescription = generateDescriptionWithGemini(product.getTitle());
                product.setDescription(aiDescription);
                System.out.println("✅ AI Description generated: " + aiDescription);
            } catch (Exception e) {
                System.err.println("❌ AI description failed: " + e.getMessage());
                String fallback = generateFallbackDescription(product.getTitle());
                product.setDescription(fallback);
                System.out.println("🔄 Using fallback description: " + fallback);
            }
        }

        Product savedProduct = productRepository.save(product);


        if (savedProduct.getVariants() == null || savedProduct.getVariants().isEmpty()) {
            createDefaultVariant(savedProduct);
        }

        return savedProduct;
    }


    public ProductVariant createDefaultVariant(Product product) {
        ProductVariant defaultVariant = new ProductVariant();
        defaultVariant.setTitle("Default Title");
        defaultVariant.setSku(generateDefaultSku(product.getTitle()));
        defaultVariant.setAllowBackorder(false);
        defaultVariant.setManageInventory(true);
        defaultVariant.setVariantRank(1);

        // FIX: Since metadata is now Map<String, Object> type, set as empty HashMap
        defaultVariant.setMetadata(new java.util.HashMap<>());

        return variantService.createVariant(product.getId(), defaultVariant);
    }


    public Product createProductWithVariants(Product product, List<ProductVariant> variants) {
        // Save the product first
        Product savedProduct = createProduct(product);

        if (variants != null && !variants.isEmpty()) {

            List<ProductVariant> existingVariants = variantService.getVariantsByProductId(savedProduct.getId());
            for (ProductVariant existingVariant : existingVariants) {
                if ("Default Title".equals(existingVariant.getTitle())) {
                    variantService.deleteVariant(existingVariant.getId());
                }
            }


            for (ProductVariant variant : variants) {
                if (variant.getMetadata() == null) {
                    variant.setMetadata(new java.util.HashMap<>());
                }
            }

            // Create the new variants
            variantService.createMultipleVariants(savedProduct.getId(), variants);
        }

        return savedProduct;
    }


    private String generateDescriptionWithGemini(String title) {
        String prompt = createDescriptionPrompt(title);
        String requestBody = createGeminiRequest(prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return extractGeminiResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate description with Gemini", e);
        }
    }

    private String createDescriptionPrompt(String title) {
        return "You are a professional e-commerce copywriter. Create a compelling product description for: \"" + title + "\"\n\n" +
                "REQUIREMENTS:\n" +
                "- Write 2-3 sentences maximum\n" +
                "- Focus on benefits and key features\n" +
                "- Use engaging, sales-oriented language\n" +
                "- Make it suitable for online store\n" +
                "- Don't use quotation marks in the response\n" +
                "- Keep it concise and professional\n\n" +
                "Product: " + title + "\n" +
                "Description:";
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
                                .replace("\\t", "\t")
                                .trim();
                    }
                }
            }
            return "Quality product crafted with attention to detail and designed for your needs.";
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Gemini response", e);
        }
    }

    // Fallback description generator if AI fails
    private String generateFallbackDescription(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "Quality product available for purchase.";
        }

        String lowerTitle = title.toLowerCase().trim();

        if (lowerTitle.contains("shirt") || lowerTitle.contains("t-shirt")) {
            return "Comfortable and stylish " + title + " made from high-quality materials. Perfect for everyday wear with excellent fit and durability.";
        }
        else if (lowerTitle.contains("mug") || lowerTitle.contains("cup")) {
            return "Premium " + title + " crafted for your daily coffee or tea enjoyment. Durable design with comfortable grip and perfect capacity.";
        }
        else if (lowerTitle.contains("phone") || lowerTitle.contains("mobile")) {
            return "Advanced " + title + " featuring cutting-edge technology and sleek design. Engineered for performance and reliability.";
        }
        else {
            return "High-quality " + title + " crafted with attention to detail. Excellent value offering reliability, style, and functionality for your needs.";
        }
    }

    public Product createProduct(String title, String description) {
        Product product = new Product();
        product.setTitle(title);
        if (description != null && !description.trim().isEmpty()) {
            product.setDescription(description);
        }

        product.setStatus("published");
        return createProduct(product);
    }

    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    // Enhanced delete method - supports both ID and title
    public boolean deleteProduct(String identifier) {
        if (deleteProductById(identifier)) {
            return true;
        }
        return deleteProductByTitle(identifier);
    }

    public boolean deleteProductById(String id) {
        if (productRepository.existsById(id)) {
            // Also delete all variants of this product
            variantService.deleteVariantsByProductId(id);
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean deleteProductByTitle(String title) {
        List<Product> products = productRepository.findByTitleContainingIgnoreCase(title);
        Optional<Product> exactMatch = products.stream()
                .filter(p -> p.getTitle().equalsIgnoreCase(title))
                .findFirst();

        if (exactMatch.isPresent()) {
            // Also delete all variants of this product
            variantService.deleteVariantsByProductId(exactMatch.get().getId());
            productRepository.delete(exactMatch.get());
            return true;
        }
        return false;
    }

    public Optional<Product> findProductByExactTitle(String title) {
        List<Product> products = productRepository.findByTitleContainingIgnoreCase(title);
        return products.stream()
                .filter(p -> p.getTitle().equalsIgnoreCase(title))
                .findFirst();
    }

    public Optional<Product> updateProductById(String id, String title, String description, String status) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();

            if (title != null && !title.trim().isEmpty()) {
                product.setTitle(title);
                product.setHandle(generateHandle(title));

                // If description is being cleared and we have a new title, regenerate description
                if (description != null && description.trim().isEmpty()) {
                    try {
                        product.setDescription(generateDescriptionWithGemini(title));
                    } catch (Exception e) {
                        product.setDescription(generateFallbackDescription(title));
                    }
                }
            }
            if (description != null && !description.trim().isEmpty()) {
                product.setDescription(description);
            }
            if (status != null && !status.trim().isEmpty()) {
                product.setStatus(status);
            }

            return Optional.of(productRepository.save(product));
        }
        return Optional.empty();
    }

    public boolean productExists(String id) {
        return productRepository.existsById(id);
    }

    public boolean productExistsByTitle(String title) {
        List<Product> products = productRepository.findByTitleContainingIgnoreCase(title);
        return products.stream().anyMatch(p -> p.getTitle().equalsIgnoreCase(title));
    }

    public long getProductCount() {
        return productRepository.count();
    }

    public long getPublishedProductCount() {
        return productRepository.findPublishedProducts().size();
    }

    // Variant-related helper methods
    public List<ProductVariant> getProductVariants(String productId) {
        return variantService.getVariantsByProductId(productId);
    }

    public long getVariantCount() {
        return variantService.getVariantCount();
    }

    public long getVariantCountForProduct(String productId) {
        return variantService.getVariantCountForProduct(productId);
    }

    private String generateHandle(String title) {
        if (title == null) return null;

        return title.toLowerCase()
                .trim()
                .replaceAll("[\\s]+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private String generateDefaultSku(String title) {
        if (title == null) return "DEFAULT-SKU";

        return title.toUpperCase()
                .trim()
                .replaceAll("[\\s]+", "-")
                .replaceAll("[^A-Z0-9-]", "")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "")
                .substring(0, Math.min(20, title.length())) + "-001";
    }
}
//POC-AI Layer