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

    @Operation(summary = "Momo payment success callback")
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam Map<String, String> params) {
        // Xây dựng URL redirect với các tham số từ MOMO
        String baseUrl = "https://swp-3-w.vercel.app"; // URL của frontend
        StringBuilder redirectUrl = new StringBuilder(baseUrl + "/staff/cart?");
        
        // Thêm các tham số quan trọng vào URL
        redirectUrl.append("status=").append("success");
        
        if (params.containsKey("orderId")) {
            redirectUrl.append("&orderId=").append(params.get("orderId"));
        }
        if (params.containsKey("amount")) {
            redirectUrl.append("&amount=").append(params.get("amount"));
        }
        if (params.containsKey("transId")) {
            redirectUrl.append("&transId=").append(params.get("transId"));
        }
        if (params.containsKey("message")) {
            redirectUrl.append("&message=").append(params.get("message"));
        }
        if (params.containsKey("resultCode")) {
            redirectUrl.append("&resultCode=").append(params.get("resultCode"));
        }

        // Script chuyển hướng
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<meta charset='UTF-8'>")
                .append("<title>Redirecting...</title>")
                .append("<script>")
                .append("window.location.href = '").append(redirectUrl).append("';")
                .append("</script>")
                .append("</head>")
                .append("<body>")
                .append("<p>Đang chuyển hướng...</p>")
                .append("</body>")
                .append("</html>");

        return htmlContent.toString();
    }


    // Momo payment success callback
    @Operation (summary = "Momo payment status callback")
    @PostMapping("/momo/ipn")
    public ResponseEntity<String> momoNotify(@RequestBody MomoIPNRequest request) {
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

    // Payment with cash
    @Operation (summary = "Payment with cash")
    @PostMapping("/cash")
    public ResponseEntity<?> paymentWithCash(@RequestBody PaymentRequest paymentRequest) throws Exception {
        Map<String, Object> result = paymentService.paymentWithCash(
                paymentRequest.getOrderId(), paymentRequest.getPaymentMethod());
        return ResponseEntity.ok(result);
    }

}

