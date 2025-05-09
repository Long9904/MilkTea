package com.src.milkTea.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.src.milkTea.enums.ProductStatusEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionResponse {
    private Long id;
    private String name;
    private String code; // Mã khuyến mãi
    private String description;
    private double minTotal; // Điều kiện áp dụng
    private double discountPercent; // % giảm (ví dụ 10 = giảm 10%)

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateOpen;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime dateEnd;

    @Enumerated(EnumType.STRING)
    private ProductStatusEnum status; // Active, deleted
}
