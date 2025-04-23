package com.src.milkTea.repository;

import com.src.milkTea.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MomoTransactionRepository extends JpaRepository<Payment, Long> {
    Optional <Payment> findByOrderId(String orderId);
}
