package com.src.milkTea.repository;

import com.src.milkTea.entities.OrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderMasterRepository extends JpaRepository<OrderMaster, Long> {
}
