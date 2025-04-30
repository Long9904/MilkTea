package com.src.milkTea.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequest {

    @NotNull(message = "Name cannot be null")
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Size(min = 0 ,max = 255, message = "description cannot exceed 255 characters")
    private String description;

    @Pattern(regexp = "^(ACTIVE|DELETED)$", message = "ACTIVE or DELETED")
    private String status;
}
