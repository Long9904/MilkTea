package com.src.milkTea.controller;

import com.src.milkTea.dto.request.PromotionRequest;
import com.src.milkTea.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/promotions")
@SecurityRequirement(name = "api")
public class PromotionAPI {

    @Autowired
    private PromotionService promotionService;

    @Operation(summary = "Create a new promotion")
    @PostMapping
    public ResponseEntity<?> createPromotion(@Valid @RequestBody PromotionRequest promotionRequest) {
        promotionService.createPromotion(promotionRequest);
        return ResponseEntity.ok("Promotion created successfully");
    }

    @Operation(summary = "Get all promotions")
    @GetMapping("/all")
    public ResponseEntity<?> getAllPromotions(@ParameterObject Pageable pageable,
                                              @RequestParam (required = false) Double price) {
        return ResponseEntity.ok(promotionService.getAllPromotions(pageable, price));

    }

    @Operation(summary = "Get promotion by id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getPromotionById(Long id) {
        return ResponseEntity.ok(promotionService.getPromotionById(id));
    }

    @Operation(summary = "Update a promotion")
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePromotion(@PathVariable Long id, @Valid @RequestBody PromotionRequest promotionRequest) {
        promotionService.updatePromotion(id, promotionRequest);
        return ResponseEntity.ok("Promotion updated successfully");
    }

    @Operation(summary = "Delete a promotion (change status to DELETED or ACTIVE)")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePromotion(@PathVariable Long id,
                                             @RequestParam(value = "status", defaultValue = "DELETED") String status) {
        promotionService.deletePromotion(id, status);
        return ResponseEntity.ok("Promotion status updated successfully");
    }

    @Operation(summary = "Check if a promotion for order is valid")
    @GetMapping("/check")
    public ResponseEntity<?> checkPromotion(@RequestParam Long promotionId,
                                            @RequestParam Long orderId) {
        return ResponseEntity.ok(promotionService.checkPromotion(promotionId, orderId));
    }

}
