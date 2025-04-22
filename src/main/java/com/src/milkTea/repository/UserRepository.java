package com.src.milkTea.repository;

import com.src.milkTea.entities.User;
import com.src.milkTea.enums.UserStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

//    boolean existsByEmail(String email);
//    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);

    Optional<User> findByIdAndStatus(Long id, UserStatusEnum status);
}
