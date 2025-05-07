package com.src.milkTea.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultToppingResponse {
    private Long toppingId;
    private String toppingName;
    private String toppingImage;
    private int quantity;
}
