package com.src.milkTea.specification;

import com.src.milkTea.entities.Product;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    // Specification cho các truy vấn tìm kiếm sản phẩm theo tên
    public static Specification<Product> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }


    // Specification cho các truy vấn tìm kiếm sản phẩm theo giá
    public static Specification<Product> priceBetween(Double minPrice, Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return criteriaBuilder.conjunction();
            } else if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("basePrice"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("basePrice"), minPrice);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("basePrice"), maxPrice);
            }
        };
    }

    // Specification cho các truy vấn tìm kiếm sản phẩm theo tên danh mục
    public static Specification<Product> categoryNameContains(String categoryName) {
        return (root, query, cb) -> {
            if (categoryName == null || categoryName.isEmpty()) return cb.conjunction();
            Join<Object, Object> categoryJoin = root.join("category", JoinType.INNER);
            return cb.like(cb.lower(categoryJoin.get("name")), "%" + categoryName.toLowerCase() + "%");
        };
    }

}
