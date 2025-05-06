package com.src.milkTea.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private Long orderId;

    @Pattern( regexp = "^(MOMO|CASH)$", message = "Payment method must be either 'MOMO' or 'CASH'" )
    private String paymentMethod;

}
