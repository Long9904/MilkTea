package com.src.milkTea.repository;

import com.src.milkTea.entities.CashDrawer;
import com.src.milkTea.entities.Payment;
import com.src.milkTea.enums.PaymentMethodEnum;
import com.src.milkTea.enums.TransactionEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

    @Query("SELECT new map(" +
           "COUNT(p) as count, " +
           "COALESCE(SUM(CAST(p.amount AS double)), 0) as total) " +
           "FROM Payment p " +
           "WHERE p.paymentMethod = :method " +
           "AND p.status = 'SUCCESS'")
    Map<String, Object> getPaymentStatsByMethod(@Param("method") PaymentMethodEnum method);

    List<Payment> findByCashDrawerAndStatus(CashDrawer drawer, TransactionEnum transactionEnum);
}
