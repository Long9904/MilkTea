package com.src.milkTea.service;

import com.src.milkTea.dto.UserDTO;
import com.src.milkTea.dto.request.UserRequest;
import com.src.milkTea.dto.response.PagingResponse;
import com.src.milkTea.entities.User;
import com.src.milkTea.enums.UserStatusEnum;
import com.src.milkTea.exception.DuplicateException;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.exception.StatusException;
import com.src.milkTea.repository.UserRepository;
import com.src.milkTea.specification.UserSpecification;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        // Take password from user and encode it
        String password = passwordEncoder.encode(userRequest.getPassword());
        updatedUserEntity.setPassword(password);
        updatedUserEntity.setId(id);
        updatedUserEntity.setUpdateAt(LocalDateTime.now());
//        updatedUserEntity.setStatus(UserStatusEnum.valueOf(userRequest.getStatus()));
        // Save the updated user
        User updatedUser = userRepository.save(updatedUserEntity);

        return modelMapper.map(updatedUser, UserDTO.class);
    }

    public PagingResponse<UserDTO> filterUsers(String name,
                                               String gender,
                                               String role,
                                               Pageable pageable) {
        // Create a specification to filter users
        Specification<User> spec = Specification
                .where(UserSpecification.nameContains(name))
                .and(UserSpecification.hasGender(gender))
                .and(UserSpecification.hasRole(role));

        // Find all users with the given specifications with pagination

        Page<User> users = userRepository.findAll(spec, pageable);

        // Convert the list of users to a list of UserDTO

        List<UserDTO> userDTOs = users.getContent().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();

        // Create a PagingResponse object to return the paginated result
        PagingResponse<UserDTO> pagingResponse = new PagingResponse<>();
        pagingResponse.setData(userDTOs);
        pagingResponse.setPage(users.getNumber());
        pagingResponse.setSize(users.getSize());
        pagingResponse.setTotalPages(users.getTotalPages());
        pagingResponse.setTotalElements(users.getTotalElements());
        pagingResponse.setLast(users.isLast());
        // Return the PagingResponse
        return pagingResponse;
    }

    public UserDTO getUserById(Long id) {
        // Check if user exists
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Convert the user entity to a UserDTO
        return modelMapper.map(user, UserDTO.class);
    }
}
