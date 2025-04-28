package com.src.milkTea.repository;

import com.src.milkTea.entities.OrderDetail;
import com.src.milkTea.entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long>, JpaSpecificationExecutor<OrderDetail> {

    @Query(value =
            "SELECT * " +
                    "FROM order_detail " +
                    "WHERE orders_id = :orderId " +
                    "ORDER BY (CASE WHEN parent_id IS NULL THEN 0 ELSE 1 END), id ASC", nativeQuery = true)
    List<OrderDetail> findByOrdersOrderByParentIdAscIdAsc(@Param("orderId") Long orderId);

    void deleteByOrders(Orders order);
}
