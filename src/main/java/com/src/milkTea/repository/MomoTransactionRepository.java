package com.src.milkTea.repository;

import com.src.milkTea.entities.MomoTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MomoTransactionRepository extends JpaRepository<MomoTransaction, Long> {
    Optional <MomoTransaction> findByOrderId(String orderId);
}
