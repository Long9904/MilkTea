package com.src.milkTea.repository;

import com.src.milkTea.entities.Category;
import com.src.milkTea.enums.ProductStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id); // Check if the name exists but not for the given ID
    Optional<Category> findByIdAndStatus(Long id, ProductStatusEnum productStatusEnum); // Find by ID and status
}
