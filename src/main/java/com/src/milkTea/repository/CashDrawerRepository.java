package com.src.milkTea.repository;

import com.src.milkTea.entities.CashDrawer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface CashDrawerRepository extends JpaRepository<CashDrawer, Long> {


    boolean existsByDateAndIsOpenTrue(LocalDate now);

    Optional<CashDrawer> findByDate(LocalDate date);

    Optional<CashDrawer> findByDateAndIsOpenTrue(LocalDate now);
}
