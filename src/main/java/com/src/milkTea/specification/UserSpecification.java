package com.src.milkTea.specification;

import com.src.milkTea.entities.Product;
import com.src.milkTea.entities.User;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.enums.UserRoleEnum;
import com.src.milkTea.enums.UserStatusEnum;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> nameContains(String fullName) {
        return (root, query, criteriaBuilder) -> {
            if (fullName == null || fullName.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%");
        };
    }

    public static Specification<User> hasStatus(UserStatusEnum status) {
        return (root, query, cb) -> {
            // mặc định sử dụng ACTIVE
            UserStatusEnum finalStatus = (status == null) ? UserStatusEnum.ACTIVE : status;
            return cb.equal(root.get("status"), finalStatus);
        };
    }

    public static Specification<User> hasGender(String gender) {
        return (root, query, cb) -> {
            if (gender == null || gender.isEmpty()) {
                return cb.conjunction(); // không lọc nếu không truyền
            }
            return cb.equal(cb.lower(root.get("gender")), gender.toLowerCase());
        };
    }

    public static Specification<User> hasRole(String role) {
        return (root, query, cb) -> {
            if (role == null || role.isEmpty()) {
                return cb.conjunction();
            }

            try {
                UserRoleEnum roleEnum = UserRoleEnum.valueOf(role.toUpperCase()); // ép kiểu từ string
                return cb.equal(root.get("role"), roleEnum);
            } catch (IllegalArgumentException e) {
                return cb.disjunction(); // nếu không parse được, trả về điều kiện false
            }
        };
    }
}
