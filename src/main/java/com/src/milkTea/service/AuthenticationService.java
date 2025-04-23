package com.src.milkTea.service;

import com.src.milkTea.dto.UserDTO;
import com.src.milkTea.dto.request.LoginRequest;
import com.src.milkTea.dto.request.RegisterRequest;
import com.src.milkTea.dto.response.LoginResponse;
import com.src.milkTea.entities.User;
import com.src.milkTea.enums.UserRoleEnum;
import com.src.milkTea.enums.UserStatusEnum;
import com.src.milkTea.exception.AuthenticationException;
import com.src.milkTea.exception.DuplicateException;
import com.src.milkTea.repository.AuthenticationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AuthenticationService implements UserDetailsService {

    @Autowired
    private AuthenticationRepository authenticationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return authenticationRepository.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    // Dùng để đăng kí tài khoản bởi admin
    public UserDTO register(RegisterRequest registerRequest) {
        /*
            1. Take user from list staff
            2. Check duplicate email
            3. Check duplicate phone
            4. Generate password
            5. Save to DB
         */
        List<String> duplicateEntries = new ArrayList<>();
        if (authenticationRepository.existsByEmail(registerRequest.getEmail())) {
            duplicateEntries.add("Email");
        }
        if (authenticationRepository.existsByPhoneNumber(registerRequest.getPhoneNumber())) {
            duplicateEntries.add("Phone number");
        }
        if(!duplicateEntries.isEmpty()) {
            throw new DuplicateException(duplicateEntries);
        }

        // Map RegisterRequest to User entity
        User user = new User();
        modelMapper.map(registerRequest, user);
        user.setPassword(passwordEncoder.encode((registerRequest.getPassword())));
        // Set Role
        if(registerRequest.getRole().equalsIgnoreCase("Manager")) {
            user.setRole(UserRoleEnum.MANAGER);
        } else if (registerRequest.getRole().equalsIgnoreCase("Staff")) {
            user.setRole(UserRoleEnum.STAFF);
        } else {
            throw new AuthenticationException("Invalid role");
        }

        // Set Status
        user.setStatus(UserStatusEnum.ACTIVE);

        // Save to DB
        authenticationRepository.save(user);
        return modelMapper.map(user, UserDTO.class);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        /*
            1. Check email, password
            2. Check status
            3. Return user info
         */
        try {
            // Authenticate user credentials by using AuthenticationManager
            authenticationManager.authenticate
                    (new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            User user = authenticationRepository.findUserByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

            if(user.getStatus().equals(UserStatusEnum.INACTIVE)){
                throw new AuthenticationException("Account is INACTIVE");
            }// User is not active

            // Generate JWT token
            String token = tokenService.generateAccessToken(user);
            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setEmail(user.getEmail());
            loginResponse.setAccessToken(token);
            loginResponse.setRole(user.getRole());
            return loginResponse;
        }
        catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Invalid username or password");
        }

    }
}
