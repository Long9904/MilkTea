package com.src.milkTea.specification;

import com.src.milkTea.entities.Category;
import com.src.milkTea.enums.ProductStatusEnum;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecification {

    public static Specification<Category> hasStatus(ProductStatusEnum status) {
        return (root, query, cb) -> {
            // mặc định sử dụng ACTIVE
            ProductStatusEnum finalStatus = (status == null) ? ProductStatusEnum.ACTIVE : status;
            return cb.equal(root.get("status"), finalStatus);
        };
    }
}
