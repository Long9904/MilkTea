package com.src.milkTea.controller;

import com.src.milkTea.dto.request.MomoIPNRequest;
import com.src.milkTea.dto.request.PaymentRequest;
import com.src.milkTea.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/payment")
@SecurityRequirement(name = "api")
public class PaymentAPI {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest paymentRequest) throws Exception {
        Map<String, Object> result = paymentService.createMomoPayment(
                paymentRequest.getOrderId(),
                paymentRequest.getPaymentMethod());
        return ResponseEntity.ok(result);
    }

    @Operation (summary = "Momo payment success callback")
    @GetMapping("/success")
    public String paymentSuccess() {
        return "Thanh toán thành công";
    }

    // Momo payment success callback
    @Operation (summary = "Momo payment status callback")
    @PostMapping("/momo/ipn")
    public ResponseEntity<String> momoNotify(@RequestBody MomoIPNRequest request) {
        System.out.println("IPN received: " + request);
        paymentService.processMomoIPN(request);
        return ResponseEntity.ok("IPN received");
    }

    // Update payment status
    @Operation (summary = "Update payment status")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable String orderId, @RequestParam String status) {
        paymentService.updatePaymentStatus(orderId, status);
        return ResponseEntity.ok("Payment status updated successfully");
    }

    // Resend payment request
    @Operation (summary = "Resend payment request")
    @PostMapping("/re-momo")
    public ResponseEntity<?> resendPaymentRequest(@RequestBody PaymentRequest paymentRequest) throws Exception {
        Map<String, Object> result = paymentService.reMomoPayment(
                paymentRequest.getOrderId());
        return ResponseEntity.ok(result);
    }

}

