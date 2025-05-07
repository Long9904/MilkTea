package com.src.milkTea.repository;

import com.src.milkTea.entities.DefaultTopping;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DefaultToppingRepository extends JpaRepository<DefaultTopping, Long> {
    void deleteAllByProductId(Long productId);

    List<DefaultTopping> findAllByProductId(Long productId);
}
