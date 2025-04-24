package com.src.milkTea.entities;

import com.src.milkTea.enums.PaymentMethodEnum;
import com.src.milkTea.enums.TransactionEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String requestId; // request id from Momo, null if not yet paid or payment method is not Momo
    private String amount;

    @Enumerated(EnumType.STRING)
    private TransactionEnum status; // pending, success, failed

    private String message; // response message from Momo or other payment methods

    private PaymentMethodEnum paymentMethod; // momo, cash, bank transfer, etc.

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
