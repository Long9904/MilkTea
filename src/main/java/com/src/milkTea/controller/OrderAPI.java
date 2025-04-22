package com.src.milkTea.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/orders")
@SecurityRequirement(name = "api")
public class OrderAPI {

    @PostMapping
    public String createOrder() {
        return "Order created successfully!";
    }

    @PostMapping("/{id}/confirm")
    public String confirmOrder() {
        return "Order confirmed successfully!";
    }

    @GetMapping("/filter")
    public String filterOrders() {
        return "Filtered orders successfully!";
    }

    @GetMapping("/{id}")
    public String getOrderById() {
        return "Order details retrieved successfully!";
    }

    // Update order status
    @PutMapping("/{id}/status")
    public String updateOrderStatus() {
        return "Order status updated successfully!";
    }

    // Huy đơn hàng cho khách hàng với status là PENDING
    @DeleteMapping("/{id}")
    public String cancelOrder() {
        return "Order cancelled successfully!";
    }

}
