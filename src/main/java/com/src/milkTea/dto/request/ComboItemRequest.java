package com.src.milkTea.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class ComboItemRequest {

    private List<Item> comboItems;

    @Data
    @Valid
    public static class Item {
        @Pattern(regexp = "^[0-9]+$", message = "Product ID must be a number")
        private String productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;

        @Pattern(regexp = "^(S|M|L|XL|NONE)$", message = "Size must be one of S, M, L, XL, NONE")
        private String size;
    }
}
