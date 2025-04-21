package com.src.milkTea.repository;

import com.src.milkTea.entities.Product;
import com.src.milkTea.enums.ProductStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByName(String name);

    boolean existsByProductCode(String productCode);

    Optional<Product> findByIdAndStatus(Long productId, ProductStatusEnum productStatusEnum);

    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByProductCodeAndIdNot(String productCode, Long id);

    @Query("SELECT p FROM Product p JOIN p.category c")
    Page<Product> findAllWithCategory(Pageable pageable);

}
