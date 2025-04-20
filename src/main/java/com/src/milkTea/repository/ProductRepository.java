package com.src.milkTea.repository;

import com.src.milkTea.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);

    boolean existsByProductCode(String productCode);
}
