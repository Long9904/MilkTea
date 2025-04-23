package com.src.milkTea.specification;

import com.src.milkTea.entities.Product;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.enums.ProductTypeEnum;
import com.src.milkTea.enums.ProductUsageEnum;
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

    public static Specification<Product> hasStatus(ProductStatusEnum status) {
        return (root, query, cb) -> {
            // mặc định sử dụng ACTIVE
            ProductStatusEnum finalStatus = (status == null) ? ProductStatusEnum.ACTIVE : status;
            return cb.equal(root.get("status"), finalStatus);
        };
    }
    // Specification cho các truy vấn tìm kiếm sản phẩm theo loại sản phẩm (productType)
    public static Specification<Product> productTypeEquals(ProductTypeEnum productType) {
        return (root, query, cb) -> {
            if (productType == null || String.valueOf(productType).isEmpty()) {
                return cb.conjunction(); // Không lọc nếu không truyền productType
            }
            return cb.equal(root.get("productType"), productType);
        };
    }


    public static Specification<Product> productUsageEquals(ProductUsageEnum productUsage) {
        return (root, query, cb) -> {
            if (productUsage == null || String.valueOf(productUsage).isEmpty()) {
                return cb.conjunction(); // Không lọc nếu không truyền productType
            }
            return cb.equal(root.get("productUsage"), productUsage);
        };
    }


}
