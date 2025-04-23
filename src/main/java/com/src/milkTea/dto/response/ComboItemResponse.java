package com.src.milkTea.dto.response;

import lombok.Data;

@Data
public class ComboItemResponse {
    private Long id;
    private Long comboId;
    private String comboName;
    private Long productId;
    private String productName;
    private int quantity;
}
