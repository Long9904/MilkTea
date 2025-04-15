package com.src.milkTea.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;

    @Schema(description = "Password of the user", example = "Password123")
    private String password;
}
