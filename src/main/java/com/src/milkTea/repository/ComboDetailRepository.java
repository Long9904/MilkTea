package com.src.milkTea.repository;

import com.src.milkTea.entities.ComboDetail;
import com.src.milkTea.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ComboDetailRepository extends JpaRepository<ComboDetail, Long> {
    void deleteByComboId(Long comboId);

    List<ComboDetail> findByComboId(Long id);

    List<ComboDetail> findByCombo(Product combo);

    /**
     * Tìm chi tiết combo dựa trên sản phẩm combo và sản phẩm con
     * @param combo Sản phẩm combo
     * @param childProduct Sản phẩm con trong combo
     * @return Chi tiết combo chứa thông tin quantity và size gốc
     */
    ComboDetail findByComboAndChildProduct(Product combo, Product childProduct);
}
