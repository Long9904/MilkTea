package com.src.milkTea.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/payments")
@SecurityRequirement(name = "api")
public class PaymentAPI {

    @PostMapping
    public String createPayment() {
        return "Payment created successfully!";
    }

    // Re choice payment method
    @PostMapping("/re-choice")
    public String reChoicePaymentMethod() {
        return "Re-chose payment method successfully!";
    }

    // Momo payment success callback
    @PostMapping("/momo/ipn")
    public ResponseEntity<String> momoNotify() {
//        momoPaymentService.processMomoIPN(request);
        return ResponseEntity.ok("IPN received");
    }
}
