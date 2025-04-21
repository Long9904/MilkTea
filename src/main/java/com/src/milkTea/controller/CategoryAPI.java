package com.src.milkTea.controller;

import com.src.milkTea.dto.request.CategoryRequest;
import com.src.milkTea.dto.response.CategoryResponse;
import com.src.milkTea.dto.response.PagingResponse;
import com.src.milkTea.dto.response.ProductResponse;
import com.src.milkTea.entities.Category;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.service.CategoryService;
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
@RequestMapping("api/category")
@SecurityRequirement(name = "api")
public class CategoryAPI {

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
        Category category = categoryService.createCategory(categoryRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest categoryRequest) {
        Category category = categoryService.updateCategory(id, categoryRequest);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Delete success");
    }

    @GetMapping
    public ResponseEntity<?> getAllCategories(@ParameterObject Pageable pageable) {
        PagingResponse<CategoryResponse> response = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getCategoryByName(@RequestParam String name, @ParameterObject Pageable pageable) {
        PagingResponse<CategoryResponse> response = categoryService.getCategoryByName(name, pageable);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Get products by category ID with status ACTIVE for UI")
    @GetMapping("/{id}/products")
    public ResponseEntity<?> getProductsByCategoryId(@PathVariable Long id, @ParameterObject Pageable pageable) {
        PagingResponse<ProductResponse> response = categoryService
                .getProductsByCategoryIdAndStatus(id, ProductStatusEnum.ACTIVE,pageable);
        return ResponseEntity.ok(response);
    }
}
