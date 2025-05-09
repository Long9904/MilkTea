package com.src.milkTea.controller;

import com.src.milkTea.dto.request.ProductRequestV2;
import com.src.milkTea.service.DefaultToppingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/products")
@SecurityRequirement(name = "api")
public class ProductAPIV2 {

    @Autowired
    private DefaultToppingService defaultToppingService;

    // Tạo 1 product kèm default topping
    @Operation(summary = "Create product with default topping")
    @PostMapping
    public ResponseEntity<?> createProductWithDefaultTopping(@Valid @RequestBody ProductRequestV2 productRequestV2) {
        defaultToppingService.createProductWithDefaultTopping(productRequestV2);
        return ResponseEntity.ok("Create product with default topping successfully");
    }

    // Update product kèm default topping
    @Operation(summary = "Update product with default topping")
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProductWithDefaultTopping(@PathVariable Long productId,
                                                              @RequestBody ProductRequestV2 productRequestV2) {
        defaultToppingService.updateProductWithDefaultTopping(productId, productRequestV2);
        return ResponseEntity.ok("Update product with default topping successfully");
    }


    // Get default topping của product bằng id
    @Operation(summary = "Get details default topping of product by product id")
    @GetMapping("/{productId}/default-topping")
    public ResponseEntity<?> getDefaultToppingByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(defaultToppingService.getDefaultToppingByProductId(productId));
    }

    // Get product kèm default topping bằng product id
    @Operation(summary = "Get product with default topping by product id")
    @GetMapping("/{productId}/default-topping/product")
    public ResponseEntity<?> getProductWithDefaultToppingByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(defaultToppingService.getProductWithDefaultToppingByProductId(productId));
    }
}
