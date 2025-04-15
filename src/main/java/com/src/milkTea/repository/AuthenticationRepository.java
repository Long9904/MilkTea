package com.src.milkTea.repository;

import com.src.milkTea.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);
}
