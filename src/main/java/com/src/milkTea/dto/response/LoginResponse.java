package com.src.milkTea.dto.response;

import com.src.milkTea.enums.UserRoleEnum;
import lombok.Data;

@Data
public class LoginResponse {
    private Long id;
    private String fullName;
    private String email;
    private UserRoleEnum role;
    private String accessToken;
}
