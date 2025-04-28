package com.src.milkTea.controller;

import com.src.milkTea.dto.request.ComboItemRequest;
import com.src.milkTea.dto.request.ComboItemRequestV2;
import com.src.milkTea.dto.request.ProductRequest;
import com.src.milkTea.dto.response.PagingResponse;
import com.src.milkTea.dto.response.ProductResponse;
import com.src.milkTea.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/products")
@SecurityRequirement(name = "api")
public class ProductAPI {

    @Autowired
    private ProductService productService;

    @Operation (summary = "Create a new product, including combo but not combo item")
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        ProductResponse productResponse = productService.createProduct(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(productResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        productService.softDeleteProduct(id);
        return ResponseEntity.ok("Delete success");
    }

    @GetMapping
    public ResponseEntity<?> getAllProducts(@ParameterObject Pageable pageable,
                                            @RequestParam(required = false) String productType,
                                            @RequestParam(required = false) String productUsage

    ) {
        PagingResponse<ProductResponse> response = productService.getAllProducts(pageable, productType, productUsage);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest) {
        ProductResponse productResponse = productService.updateProduct(id, productRequest);
        return ResponseEntity.ok(productResponse);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterProducts(@ParameterObject Pageable pageable,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) Double minPrice,
                                            @RequestParam(required = false) Double maxPrice,
                                            @RequestParam(required = false) String categoryName,
                                            @RequestParam(required = false) String productType,
                                            @RequestParam(required = false) String productUsage
    ) {
        PagingResponse<ProductResponse> response =
                productService.filterProducts(name, minPrice, maxPrice, categoryName,productType, productUsage, pageable);
        return ResponseEntity.ok(response);
    }

    // Update combo details
    @Operation(summary = "Update or add combo details for a product with type COMBO")
    @PutMapping("{comboId}/combo")
    public ResponseEntity<?> updateCombo(@PathVariable Long comboId,
                                         @Valid @RequestBody ComboItemRequest comboItemRequest) {

        productService.updateComboItem(comboId, comboItemRequest);
        return ResponseEntity.ok("Update combo success");
    }

    // Get combo details by productId
    @Operation(summary = "Get combo details by productId")
    @GetMapping("{productId}/combo")
    public ResponseEntity<?> getComboByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getComboByProductId(productId));
    }

    // Xem product by id
    @Operation(summary = "Get product by id")
    @GetMapping("{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductById(productId));
    }

    @Operation(summary = "Post combo with detail - version 2")
    @PostMapping("v2/{comboId}/combo")
    public ResponseEntity<?> updateComboV2(@PathVariable Long comboId,
                                           @Valid @RequestBody ComboItemRequestV2 comboItemRequestV2) {

        return ResponseEntity.ok(productService.updateComboWithDetail(comboId, comboItemRequestV2));
    }

    @Operation(summary = "Get combo with detail by productId - version 2")
    @GetMapping("v2/{productId}")
    public ResponseEntity<?> getProductByIdV2(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.getProductByIdV2(productId));
    }

    // Create combo with detail - version 2
    @Operation(summary = "Create combo with detail - version 2")
    @PostMapping("v2")
    public ResponseEntity<?> createComboV2(@Valid @RequestBody ComboItemRequestV2 comboItemRequestV2) {
        return ResponseEntity.ok(productService.createComboWithDetail(comboItemRequestV2));
    }
}
