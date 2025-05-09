package com.src.milkTea.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionRequest {

    @NotNull(message = "name cannot be null")
    private String name;

    @NotNull(message = "code cannot be null")
    private String code; // Mã khuyến mãi

    private String description;

    @Min(value = 0, message = "minTotal must be greater than or equal to 0")
    private double minTotal; // Điều kiện áp dụng

    @Min(value = 0, message = "discountPercent must be greater than or equal to 0")
    @Max(value = 100, message = "discountPercent must be less than or equal to 100")
    private double discountPercent; // % giảm (ví dụ 10 = giảm 10%)

    @NotNull(message = "dateOpen cannot be null")
    @Schema(description = "Date format: yyyy-MM-dd HH:mm:ss", example = "2023-10-01 12:00:00")
    private String dateOpen;

    @NotNull(message = "dateEnd cannot be null")
    @Schema(description = "Date format: yyyy-MM-dd HH:mm:ss", example = "2023-10-31 12:00:00")
    private String dateEnd;
}
