package com.src.milkTea.controller;

import com.src.milkTea.dto.request.OrderRequest;
import com.src.milkTea.service.OrderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
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
        System.out.println(orderRequest);  // Kiểm tra dữ liệu nhận được
        orderService.createOrder(orderRequest);
        return ResponseEntity.ok(orderRequest);
    }
}
