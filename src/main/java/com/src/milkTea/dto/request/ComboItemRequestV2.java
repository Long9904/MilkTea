package com.src.milkTea.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComboItemRequestV2 {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
    private String name;

    @Positive(message = "Base price must be positive")
    @DecimalMin(value = "0.0", message = "Base price must be greater than or equal to 0")
    private Double basePrice;

    @NotBlank(message = "Product code is required")
    private String productCode;

    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @NotBlank(message = "Product type is required")
    @Pattern(regexp = "^(COMBO)$", message = "Product type must be 'COMBO'")
    private String productType;

    @Pattern(regexp = "^(ACTIVE|DELETED)$", message = "ACTIVE or DELETED")
    private String status;

    @NotBlank(message = "Product usage is required")
    @Pattern(regexp = "^(MAIN)$", message = "Product usage must be 'MAIN'")
    private String productUsage;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private List<ComboItemRequest.Item> comboItems;

    @Data
    @Valid
    public static class Item {
        @Pattern(regexp = "^[0-9]+$", message = "Product ID must be a number")
        private String productId;

        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;

        @Null(message = "Size can be null or one of S, M, or L")
        @Pattern(regexp = "^[SML]$", message = "Size must be one of S, M, or L")
        private String size;
    }
}
