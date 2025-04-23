package com.src.milkTea.repository;

import com.src.milkTea.entities.ComboDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComboDetailRepository extends JpaRepository<ComboDetail, Long> {
    void deleteByComboId(Long comboId);

    List<ComboDetail> findByComboId(Long id);
}
