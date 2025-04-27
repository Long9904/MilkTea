package com.src.milkTea.specification;

import com.src.milkTea.entities.Orders;
import com.src.milkTea.enums.OrderStatusEnum;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;


public class OrderSpecification {

    // Truy vấn tìm kiếm đơn hàng theo totalPrice
    public static Specification<Orders> priceBetween(Double minPrice, Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null && maxPrice == null) {
                return criteriaBuilder.conjunction();
            } else if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("totalPrice"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("totalPrice"), minPrice);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("totalPrice"), maxPrice);
            }
        };
    }

    public static Specification<Orders> orderStatus(OrderStatusEnum orderStatus) {
        return (root, query, cb) -> {
            if (orderStatus == null || String.valueOf(orderStatus).isEmpty()) {
                return cb.conjunction(); // Không lọc nếu không truyền productType
            }
            return cb.equal(root.get("status"), orderStatus);
        };
    }

    // Specification cho các truy vấn tìm kiếm sản phẩm theo tên danh mục
    public static Specification<Orders> staffNameContains(String staffName) {
        return (root, query, cb) -> {
            if (staffName == null || staffName.isEmpty()) return cb.conjunction();
            Join<Object, Object> categoryJoin = root.join("user", JoinType.INNER);
            return cb.like(cb.lower(categoryJoin.get("fullName")), "%" + staffName.toLowerCase() + "%");
        };
    }
}