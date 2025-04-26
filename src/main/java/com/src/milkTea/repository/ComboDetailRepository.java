package com.src.milkTea.repository;

import com.src.milkTea.entities.ComboDetail;
import com.src.milkTea.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComboDetailRepository extends JpaRepository<ComboDetail, Long> {
    void deleteByComboId(Long comboId);

    List<ComboDetail> findByComboId(Long id);

    List<ComboDetail> findByCombo(Product combo);
}
