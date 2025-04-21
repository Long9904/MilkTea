package com.src.milkTea.repository;

import com.src.milkTea.entities.Category;
import com.src.milkTea.enums.ProductStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id); // Check if the name exists but not for the given ID
    Optional<Category> findByIdAndStatus(Long id, ProductStatusEnum productStatusEnum); // Find by ID and status
    Page<Category> findByNameContainingIgnoreCaseAndStatus(String name, Pageable pageable, ProductStatusEnum status); // Find by name and status
}
