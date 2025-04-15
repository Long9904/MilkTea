package com.src.milkTea.controller;

import com.src.milkTea.dto.UserDTO;
import com.src.milkTea.dto.request.RegisterRequest;
import com.src.milkTea.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/authentication")
//@SecurityRequirement(name = "api")
public class AuthenticationAPI {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserDTO userDTO = authenticationService.register(registerRequest);
        return ResponseEntity.ok(userDTO);
    }


}
