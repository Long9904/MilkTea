package com.src.milkTea.service;

import com.src.milkTea.dto.UserDTO;
import com.src.milkTea.dto.request.RegisterRequest;
import com.src.milkTea.entities.User;
import com.src.milkTea.enums.UserRoleEnum;
import com.src.milkTea.enums.UserStatusEnum;
import com.src.milkTea.exception.DuplicateException;
import com.src.milkTea.repository.AuthenticationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final ModelMapper modelMapper;

    public AuthenticationService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    // Dùng để đăng kí tài khoản bởi admin --> tự generate info sau đó staff sẽ tự động update
    public UserDTO register(RegisterRequest registerRequest) {
        /*
            1. Take email, phone, full from list staff
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

        User user = new User();
        modelMapper.map(registerRequest, user);
        user.setPassword(passwordEncoder.encode("Password123"));
        user.setRole(UserRoleEnum.STAFF);
        user.setStatus(UserStatusEnum.INACTIVE); // ACTIVE when staff update info
        authenticationRepository.save(user);
        return modelMapper.map(user, UserDTO.class);
    }
}
