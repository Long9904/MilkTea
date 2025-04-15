package com.src.milkTea.dto.response;

import com.src.milkTea.enums.UserRoleEnum;
import lombok.Data;

@Data
public class LoginResponse {
    private String email;
    private UserRoleEnum role;
    private String accessToken;
}
