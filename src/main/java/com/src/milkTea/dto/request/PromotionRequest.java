package com.src.milkTea.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionRequest {

    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotBlank(message = "code cannot be blank")
    private String code; // Mã khuyến mãi

    private String description;

    @Min(value = 0, message = "minTotal must be greater than or equal to 0")
    private double minTotal; // Điều kiện áp dụng

    @Min(value = 0, message = "discountPercent must be greater than or equal to 0")
    @Max(value = 100, message = "discountPercent must be less than or equal to 100")
    private double discountPercent; // % giảm (ví dụ 10 = giảm 10%)

    @NotBlank(message = "dateOpen cannot be null")
    @Schema(description = "Date format: yyyy/MM/dd HH:mm:ss", example = "2023/10/01 12:00:00")
    private String dateOpen;

    @NotBlank(message = "dateEnd cannot be null")
    @Schema(description = "Date format: yyyy/MM/dd HH:mm:ss", example = "2023/10/31 12:00:00")
    private String dateEnd;
}
