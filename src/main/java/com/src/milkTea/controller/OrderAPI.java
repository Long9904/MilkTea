package com.src.milkTea.controller;

import com.src.milkTea.dto.request.OrderRequest;
import com.src.milkTea.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/orders")
@SecurityRequirement(name = "api")
public class OrderAPI {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.addItemToCart(orderRequest));
    }

    @Operation(summary = "Get all orders by conditions")
    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String staffName,
            @RequestParam(required = false) String paymentMethod
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(minPrice, maxPrice, status, staffName, paymentMethod ,pageable));
    }

    @Operation(summary = "Get order details by id")
    @GetMapping("/{id}/details")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderDetails(id));
    }

    // Update order status
    @Operation(summary = "Update order status")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok("Order status updated successfully");
    }
}
