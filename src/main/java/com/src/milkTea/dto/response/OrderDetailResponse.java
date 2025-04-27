package com.src.milkTea.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    private Long id;
    private String productName;
    private int quantity;
    private double unitPrice;
    private String size;
    private String note;
    private boolean isCombo;
    private List<OrderDetailResponse> childItems = new ArrayList<>();
}
