package com.src.milkTea.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
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
    @Pattern(regexp = "^(SINGLE|COMBO)$", message = "Product type must be 'SINGLE' or 'COMBO'")
    private String productType;

    @NotBlank(message = "Product usage is required")
    @Pattern(regexp = "^(MAIN|EXTRA)$", message = "Product usage must be 'MAIN' or 'EXTRA'")
    private String productUsage;

    @NotNull(message = "Category is required")
    private Long categoryId;
}
