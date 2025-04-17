package com.src.milkTea.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Schema(description = "Email", example = "registerEmail@gmail.com")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "Full name of the user", example = "Long Cha")
    @Size(min = 1, max = 30, message = "Full name must be between 1 and 30 characters")
    private String fullName;

    @Schema(description = "Phone number", example = "0841234567")
    @Pattern(regexp = "(84|0[35789])[0-9]{8}", message = "Invalid phone number")
    private String phoneNumber;

    @Schema(description = "Staff", example = "Male")
    @Pattern(regexp = "^(Staff|Manager)$", message = "Manager or Staff")
    private String role;
}
