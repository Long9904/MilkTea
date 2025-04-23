package com.src.milkTea.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailRequest {

    private Long productId;            // Topping hoặc trà trong combo

    // Size của sp khi đặt sp với productType là combo
    @Pattern(regexp = "^([SML])?$", message = "Size must be one of S, M, or L")
    private String size;// Có thể null nếu là topping

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be greater than 0")
    private int quantity;

}
