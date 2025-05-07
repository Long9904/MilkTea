package com.src.milkTea.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.enums.ProductTypeEnum;
import com.src.milkTea.enums.ProductUsageEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseV2 {
    private Long id;
    private String name;
    private Double basePrice;
    private String productCode;
    private String imageUrl;
    private String description;

    @Enumerated(EnumType.STRING)
    private ProductTypeEnum productType;

    @Enumerated(EnumType.STRING)
    private ProductUsageEnum productUsage;

    @Enumerated(EnumType.STRING)
    private ProductStatusEnum status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime updateAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime deleteAt;

    private Long categoryId;

    private String categoryName;

    private List<DefaultToppingResponse> defaultToppings;
}
