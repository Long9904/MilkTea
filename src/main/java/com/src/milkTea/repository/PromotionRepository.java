package com.src.milkTea.repository;

import com.src.milkTea.entities.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion> {

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

}
