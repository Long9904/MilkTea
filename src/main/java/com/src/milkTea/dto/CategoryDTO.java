package com.src.milkTea.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {


    @NotNull(message = "Name cannot be null")
    private String name;

    @Size(min = 0 ,max = 255, message = "Address cannot exceed 255 characters")
    private String description;
}
