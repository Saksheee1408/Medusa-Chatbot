package org.example.Controller;

import org.example.Model.Product;
import org.example.Repository.ProductRepository;
import org.example.Repository.ProductVariantRepository;
import org.example.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;

    @Autowired
    private ProductService productService;

    public ProductController(ProductRepository productRepo, ProductVariantRepository variantRepo) {
        this.productRepo = productRepo;
        this.variantRepo = variantRepo;
    }


    @GetMapping("/stock/{productName}")
    public Map<String, Object> getProductStock(@PathVariable String productName) {
        Integer totalStock = variantRepo.getTotalStockByProductName(productName);
        Map<String, Object> response = new HashMap<>();
        response.put("productName", productName);
        response.put("totalStock", totalStock != null ? totalStock : 0);
        return response;
    }

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productRepo.findPublishedProducts();
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("ProductController is working!");
    }


    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String title) {
        List<Product> products = productService.searchProductsByTitle(title);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/all-including-draft")
    public ResponseEntity<List<Product>> getAllProductsIncludingDraft() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        if (!productService.productExists(id)) {
            return ResponseEntity.notFound().build();
        }
        product.setId(id);
        Product updatedProduct = productService.updateProduct(product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        if (productService.deleteProduct(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}