package com.src.milkTea.specification;

import com.src.milkTea.entities.Promotion;
import org.springframework.data.jpa.domain.Specification;

public class PromotionSpecification {

    // Truy vấn tìm kiếm khuyến mãi có minPrice <= price

    public static Specification<Promotion>  priceMoreThan(Double price) {
        return (root, query, criteriaBuilder) -> {
            if (price == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("minTotal"), price);
        };
    }
}
