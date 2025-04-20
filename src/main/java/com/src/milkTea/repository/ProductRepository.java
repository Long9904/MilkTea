package com.src.milkTea.repository;

import com.src.milkTea.entities.Product;
import com.src.milkTea.enums.ProductStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);

    boolean existsByProductCode(String productCode);

    Optional<Product> findByIdAndStatus(Long productId, ProductStatusEnum productStatusEnum);
}
