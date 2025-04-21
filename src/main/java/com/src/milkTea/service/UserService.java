package com.src.milkTea.service;

import com.src.milkTea.dto.UserDTO;
import com.src.milkTea.dto.request.UserRequest;
import com.src.milkTea.entities.User;
import com.src.milkTea.enums.UserStatusEnum;
import com.src.milkTea.exception.DuplicateException;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.exception.StatusException;
import com.src.milkTea.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    public void softDeleteUser(Long id) {
        // Check if user is already deleted
        User user = userRepository.findByIdAndStatus(id, UserStatusEnum.DELETED)
                .orElseThrow(() -> new StatusException("User is already deleted"));

        // Set user status to DELETED
        User deletedUser = userRepository.findByIdAndStatus(id, UserStatusEnum.ACTIVE)
                .orElseThrow(() -> new NotFoundException("User not found"));
        deletedUser.setStatus(UserStatusEnum.DELETED);
        deletedUser.setDeleteAt(LocalDateTime.now());
        userRepository.save(deletedUser);
    }

    public UserDTO updateUser(Long id, UserRequest userRequest) {
        // Check if user exists
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check duplicate email, phone number
        List<String> duplicates = new ArrayList<>();
        if (userRepository.existsByEmailAndIdNot(userRequest.getEmail(), id)) {
            duplicates.add("Email already exists");
        }
        if (userRepository.existsByPhoneNumberAndIdNot(userRequest.getPhoneNumber(), id)) {
            duplicates.add("Phone number already exists");
        }
        if (!duplicates.isEmpty()) {
            throw new DuplicateException(duplicates);
        }

        User updatedUserEntity = modelMapper.map(userRequest, User.class);
        // Set the existing ID to ensure we're updating the existing user
        updatedUserEntity.setId(id);
        updatedUserEntity.setUpdateAt(LocalDateTime.now());
        // Save the updated user
        User updatedUser = userRepository.save(updatedUserEntity);

        return modelMapper.map(updatedUser, UserDTO.class);
    }
}
