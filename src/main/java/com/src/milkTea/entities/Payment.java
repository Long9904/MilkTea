package com.src.milkTea.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @JsonIgnore
    private Orders order; // order id from Orders table

    private String requestId; // request id from Momo, null if not yet paid or payment method is not Momo
    private String amount;

    @Enumerated(EnumType.STRING)
    private TransactionEnum status; // pending, success, failed

    private String message; // response message from Momo or other payment methods

    @Enumerated(EnumType.STRING)
    private PaymentMethodEnum paymentMethod; // momo, cash, bank transfer, etc.

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Liên kết với CashDrawer
    @ManyToOne
    @JoinColumn(name = "cash_drawer_id", referencedColumnName = "id")
    private CashDrawer cashDrawer; // cash drawer id from CashDrawer table

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
