package com.src.milkTea.repository;


import com.src.milkTea.entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Long>, JpaSpecificationExecutor<Orders> {

    // Thống kê theo ngày
    @Query("SELECT DATE(o.createAt) AS orderDate, " +
            "COUNT(o.id) AS totalOrders, " +
            "SUM(o.totalPrice) AS totalRevenue " +
            "FROM Orders o " +
            "WHERE o.status = com.src.milkTea.enums.OrderStatusEnum.PAID OR o.status = com.src.milkTea.enums.OrderStatusEnum.DELIVERED OR o.status = com.src.milkTea.enums.OrderStatusEnum.PREPARING " +
            "GROUP BY DATE(o.createAt) " +
            "ORDER BY orderDate DESC")
    List<Object[]> getRevenueByDay();

    // Thống kê theo tuần
    @Query("SELECT DATE_FORMAT(o.createAt, '%Y-%u') AS week, " +
            "COUNT(o.id) AS totalOrders, " +
            "SUM(o.totalPrice) AS totalRevenue " +
            "FROM Orders o " +
            "WHERE o.status = com.src.milkTea.enums.OrderStatusEnum.PAID OR o.status = com.src.milkTea.enums.OrderStatusEnum.DELIVERED OR o.status = com.src.milkTea.enums.OrderStatusEnum.PREPARING " +
            "GROUP BY DATE_FORMAT(o.createAt, '%Y-%u') " +
            "ORDER BY week DESC")
    List<Object[]> getRevenueByWeek();

    // Thống kê theo tháng
    @Query("SELECT DATE_FORMAT(o.createAt, '%Y-%m') AS month, " +
            "COUNT(o.id) AS totalOrders, " +
            "SUM(o.totalPrice) AS totalRevenue " +
            "FROM Orders o " +
            "WHERE o.status = com.src.milkTea.enums.OrderStatusEnum.PAID OR o.status = com.src.milkTea.enums.OrderStatusEnum.DELIVERED OR o.status = com.src.milkTea.enums.OrderStatusEnum.PREPARING " +
            "GROUP BY DATE_FORMAT(o.createAt, '%Y-%m') " +
            "ORDER BY month DESC")
    List<Object[]> getRevenueByMonth();

    // Thống kê theo năm
    @Query("SELECT YEAR(o.createAt) AS year, " +
            "COUNT(o.id) AS totalOrders, " +
            "SUM(o.totalPrice) AS totalRevenue " +
            "FROM Orders o " +
            "WHERE o.status = com.src.milkTea.enums.OrderStatusEnum.PAID OR o.status = com.src.milkTea.enums.OrderStatusEnum.DELIVERED OR o.status = com.src.milkTea.enums.OrderStatusEnum.PREPARING " +
            "GROUP BY YEAR(o.createAt) " +
            "ORDER BY year DESC")
    List<Object[]> getRevenueByYear();

    @Query("SELECT sum(o.totalPrice) FROM Orders o WHERE o.status = com.src.milkTea.enums.OrderStatusEnum.PAID")
    Long totalMoneyAll(); // Tổng tiền của tất cả đơn hàng đã thanh toán
}
