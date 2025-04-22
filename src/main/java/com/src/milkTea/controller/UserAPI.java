package com.src.milkTea.controller;

import com.src.milkTea.dto.UserDTO;
import com.src.milkTea.dto.request.UserRequest;
import com.src.milkTea.dto.response.PagingResponse;
import com.src.milkTea.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/users")
@SecurityRequirement(name = "api")
public class UserAPI {

    @Autowired
    private UserService userService;

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequest userRequest) {
        UserDTO updatedUser = userService.updateUser(id, userRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // Filter users by name, gender, and role
    @GetMapping("/filter")
    public ResponseEntity<?> filterAllUsers(@ParameterObject Pageable pageable,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) String gender,
                                            @RequestParam(required = false) String role) {
        PagingResponse<UserDTO> userDTOs = userService.filterUsers(name,gender,role,pageable);
        return ResponseEntity.ok(userDTOs);
    }

}
