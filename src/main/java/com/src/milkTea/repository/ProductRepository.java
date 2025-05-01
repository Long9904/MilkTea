package com.src.milkTea.repository;

import com.src.milkTea.entities.Product;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.enums.ProductTypeEnum;
import com.src.milkTea.enums.ProductUsageEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    boolean existsByName(String name);

    boolean existsByProductCode(String productCode);

    Optional<Product> findByIdAndStatus(Long productId, ProductStatusEnum productStatusEnum);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByProductCodeAndIdNot(String productCode, Long id);

    Page<Product> findByCategoryIdAndStatus(Long categoryId, ProductStatusEnum status ,Pageable pageable);

    @Query("SELECT p FROM Product p JOIN FETCH p.category c WHERE p.id = :id")
    Optional<Product> findByIdWithCategory(Long id);

    @Query("SELECT count(p) FROM Product p WHERE p.productType = :productType AND p.productUsage = :productUsage")
    long countByProductTypeAndProductUsage(ProductUsageEnum productUsage, ProductTypeEnum productType);

    // top 3 best-selling products (name, quantity)
    @Query("select p.name, sum(od.quantity) as totalSold, p.imageUrl " +
            "from OrderDetail od " +
            "join od.product p " +
            "where p.productUsage = 'MAIN' " +
            "and od.parent is null " +
            "group by p.id " +
            "order by totalSold desc " +
            "limit 3")
    List<Object[]> findTop3BestSellingProduct();

    // top 3 best-selling products by product usage (name, quantity)
    @Query("select p.name, sum(od.quantity) as totalSold, p.imageUrl " +
            "from OrderDetail od " +
            "join od.product p " +
            "where p.productUsage = :productUsageEnum " +
            "and od.parent is null " +
            "group by p.id " +
            "order by totalSold desc " +
            "limit 3")
    List<Object[]> findTop3BestSellingProductByProductUsage(ProductUsageEnum productUsageEnum);

    @Query("select p.name, sum(od.quantity) as totalSold, p.imageUrl " +
            "from OrderDetail od " +
            "join od.product p " +
            "where p.productUsage = :productUsageEnum " +
            "group by p.id " +
            "order by totalSold desc " +
            "limit 3")
    List<Object[]> findTop3ExtraProduct(ProductUsageEnum productUsageEnum);
}
