package com.src.milkTea.controller;

import com.src.milkTea.dto.request.OrderItemRequest;
import com.src.milkTea.dto.request.OrderRequest;
import com.src.milkTea.service.OrderService;
import com.src.milkTea.service.OrderServiceV2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/orders")
@SecurityRequirement(name = "api")
public class OrderAPIV2 {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderServiceV2 orderServiceV2;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderServiceV2.addItemToCart(orderRequest));
    }


    @PutMapping("/{orderId}")
    @Operation(summary = "Add items to an existing order")
    public ResponseEntity<?> addItemToOrder(@PathVariable Long orderId, @RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderServiceV2.addItemToExistingOrder(orderId, orderRequest));
    }

    @DeleteMapping("/{orderId}/details/{orderDetailId}")
    @Operation(summary = "Delete a specific item from an order")
    public ResponseEntity<?> deleteOrderDetail(@PathVariable Long orderId, @PathVariable Long orderDetailId) {
        orderServiceV2.deleteOrderDetail(orderId, orderDetailId);
        return ResponseEntity.ok("Order item deleted successfully");
    }

    // API cũ V1
    @Operation(summary = "Get all orders by conditions")
    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String staffName,
            @RequestParam(required = false) String paymentMethod
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(minPrice, maxPrice, status, staffName, paymentMethod, pageable));
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

    @PutMapping("/{orderId}/details/{orderDetailId}/update")
    @Operation(summary = "Update an order detail by overwriting")
    public ResponseEntity<?> updateOrderDetail(
            @PathVariable Long orderId,
            @PathVariable Long orderDetailId,
            @RequestParam String size,
            @RequestParam int quantity){
        return ResponseEntity.ok(orderServiceV2.updateOrderDetail(orderId, orderDetailId, size, quantity));
    }

}
