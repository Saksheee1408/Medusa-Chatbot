package org.example.Controller;

import org.example.Model.ProductVariant;
import org.example.Service.ProductVariantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/variants")
@CrossOrigin(origins = "*")
public class ProductVariantController {

    @Autowired
    private ProductVariantService variantService;


    @PostMapping("/product/{productId}")
    public ResponseEntity<?> createVariant(
            @PathVariable String productId,
            @RequestBody ProductVariant variant) {
        try {
            ProductVariant createdVariant = variantService.createVariant(productId, variant);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Variant created successfully",
                    "variant", createdVariant
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to create variant: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/product/{productId}/simple")
    public ResponseEntity<?> createSimpleVariant(
            @PathVariable String productId,
            @RequestParam String title,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String barcode) {
        try {
            ProductVariant createdVariant = variantService.createVariant(productId, title, sku, barcode);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Variant created successfully",
                    "variant", createdVariant
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to create variant: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/product/{productId}/bulk")
    public ResponseEntity<?> createMultipleVariants(
            @PathVariable String productId,
            @RequestBody List<ProductVariant> variants) {
        try {
            List<ProductVariant> createdVariants = variantService.createMultipleVariants(productId, variants);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Variants created successfully",
                    "variants", createdVariants,
                    "count", createdVariants.size()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to create variants: " + e.getMessage()
            ));
        }
    }

    // READ operations
    @GetMapping
    public ResponseEntity<List<ProductVariant>> getAllVariants() {
        try {
            List<ProductVariant> variants = variantService.getAllVariants();
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProductVariant>> getActiveVariants() {
        try {
            List<ProductVariant> variants = variantService.getActiveVariants();
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{variantId}")
    public ResponseEntity<ProductVariant> getVariantById(@PathVariable String variantId) {
        try {
            Optional<ProductVariant> variant = variantService.getVariantById(variantId);
            return variant.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVariant>> getVariantsByProductId(@PathVariable String productId) {
        try {
            List<ProductVariant> variants = variantService.getVariantsByProductId(productId);
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/product/{productId}/active")
    public ResponseEntity<List<ProductVariant>> getActiveVariantsByProductId(@PathVariable String productId) {
        try {
            List<ProductVariant> variants = variantService.getActiveVariantsByProductId(productId);
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<ProductVariant>> searchVariantsByTitle(@RequestParam String title) {
        try {
            List<ProductVariant> variants = variantService.searchVariantsByTitle(title);
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/sku")
    public ResponseEntity<List<ProductVariant>> searchVariantsBySku(@RequestParam String sku) {
        try {
            List<ProductVariant> variants = variantService.searchVariantsBySku(sku);
            return ResponseEntity.ok(variants);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/find/sku/{sku}")
    public ResponseEntity<ProductVariant> findVariantBySku(@PathVariable String sku) {
        try {
            Optional<ProductVariant> variant = variantService.findVariantBySku(sku);
            return variant.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/find/barcode/{barcode}")
    public ResponseEntity<ProductVariant> findVariantByBarcode(@PathVariable String barcode) {
        try {
            Optional<ProductVariant> variant = variantService.findVariantByBarcode(barcode);
            return variant.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // UPDATE operations
    @PutMapping("/{variantId}")
    public ResponseEntity<?> updateVariant(
            @PathVariable String variantId,
            @RequestBody ProductVariant updates) {
        try {
            Optional<ProductVariant> updatedVariant = variantService.updateVariant(variantId, updates);
            if (updatedVariant.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Variant updated successfully",
                        "variant", updatedVariant.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to update variant: " + e.getMessage()
            ));
        }
    }

    @PatchMapping("/{variantId}")
    public ResponseEntity<?> updateVariantBasicInfo(
            @PathVariable String variantId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String barcode) {
        try {
            Optional<ProductVariant> updatedVariant = variantService.updateVariantBasicInfo(variantId, title, sku, barcode);
            if (updatedVariant.isPresent()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Variant updated successfully",
                        "variant", updatedVariant.get()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to update variant: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/product/{productId}/reorder")
    public ResponseEntity<?> reorderVariants(
            @PathVariable String productId,
            @RequestBody List<String> variantIds) {
        try {
            boolean success = variantService.reorderVariants(productId, variantIds);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Variants reordered successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Failed to reorder variants"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to reorder variants: " + e.getMessage()
            ));
        }
    }

    // DELETE operations
    @DeleteMapping("/{variantId}")
    public ResponseEntity<?> deleteVariant(@PathVariable String variantId) {
        try {
            boolean deleted = variantService.deleteVariant(variantId);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Variant deleted successfully"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to delete variant: " + e.getMessage()
            ));
        }
    }

    @PatchMapping("/{variantId}/soft-delete")
    public ResponseEntity<?> softDeleteVariant(@PathVariable String variantId) {
        try {
            boolean deleted = variantService.softDeleteVariant(variantId);
            if (deleted) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Variant soft deleted successfully"
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to soft delete variant: " + e.getMessage()
            ));
        }
    }

    // UTILITY operations
    @GetMapping("/{variantId}/exists")
    public ResponseEntity<Map<String, Boolean>> checkVariantExists(@PathVariable String variantId) {
        try {
            boolean exists = variantService.variantExists(variantId);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("exists", false));
        }
    }

    @GetMapping("/check/sku/{sku}/exists")
    public ResponseEntity<Map<String, Boolean>> checkSkuExists(@PathVariable String sku) {
        try {
            boolean exists = variantService.skuExists(sku);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("exists", false));
        }
    }

    @GetMapping("/check/barcode/{barcode}/exists")
    public ResponseEntity<Map<String, Boolean>> checkBarcodeExists(@PathVariable String barcode) {
        try {
            boolean exists = variantService.barcodeExists(barcode);
            return ResponseEntity.ok(Map.of("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("exists", false));
        }
    }

    // STATISTICS
    @GetMapping("/stats")
    public ResponseEntity<?> getVariantStatistics() {
        try {
            long totalVariants = variantService.getVariantCount();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "statistics", Map.of(
                            "totalVariants", totalVariants
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to get statistics: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<?> getProductVariantStatistics(@PathVariable String productId) {
        try {
            long variantCount = variantService.getVariantCountForProduct(productId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "productId", productId,
                    "statistics", Map.of(
                            "variantCount", variantCount
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Failed to get statistics: " + e.getMessage()
            ));
        }
    }
}