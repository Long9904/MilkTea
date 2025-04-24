package com.src.milkTea.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    @Schema(description = "Full name of the user", example = "Long Cha")
    @Size(min = 1, max = 30, message = "Full name must be between 1 and 30 characters")
    private String fullName;

    @Schema(description = "Email", example = "registerEmail@gmail.com")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "Password", example = "Password123")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, and one digit")
    private String password;

    @Schema(description = "Date of birth (yyyy-mm-dd)", example = "2023-01-30")
    @NotNull(message = "Date of birth is required")
    @Past(message = "Invalid date of birth")
    private LocalDate dateOfBirth;

    @Schema(description = "Male|Female|Other", example = "Male")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be 'Male', 'Female', or 'Other'")
    @NotBlank(message = "Gender is required")
    private String gender;

    @Schema(description = "Phone number", example = "0841234567")
    @Pattern(regexp = "(84|0[35789])[0-9]{8}", message = "Invalid phone number")
    private String phoneNumber;

    @Schema(description = "Address", example = "HCM City")
    @Size(min = 0 ,max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @Pattern(regexp = "^(STAFF|MANAGER)$", message = "Role must be 'STAFF' or 'MANAGER'")
    private String role;

    @Pattern(regexp = "^(ACTIVE|DELETED)$", message = "Status must be 'ACTIVE' or 'DELETED'")
    private String status;

}
