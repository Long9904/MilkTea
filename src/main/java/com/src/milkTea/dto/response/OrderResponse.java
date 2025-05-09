package com.src.milkTea.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.src.milkTea.enums.OrderStatusEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private Long id;

    private double totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatusEnum status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime updateAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime deleteAt;

    private Long userId;

    private String userName;

    private String paymentMethod;

    private String amountPaid;

    private String discountCode;

    private double discountPercent;
}

