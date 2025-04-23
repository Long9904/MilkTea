package com.src.milkTea.dto.request;

import lombok.Data;

@Data
public class ComboItemRequest {
    private Long productId;
    private int quantity;

    private String size;
}
