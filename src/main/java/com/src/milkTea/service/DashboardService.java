package com.src.milkTea.service;

import com.src.milkTea.enums.ProductTypeEnum;
import com.src.milkTea.enums.ProductUsageEnum;
import com.src.milkTea.enums.UserRoleEnum;
import com.src.milkTea.repository.OrderRepository;
import com.src.milkTea.repository.ProductRepository;
import com.src.milkTea.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Map<String, Object> getDashBoardStats() {

        Map<String, Object> stats = new HashMap<>();

        // count all products (with all status)
        Long allProducts = productRepository.count();
        stats.put("totalProduct", allProducts);

        // count combo products (productType = combo, productUsage = main)
        Long comboProducts = productRepository.countByProductTypeAndProductUsage(ProductUsageEnum.MAIN, ProductTypeEnum.COMBO);
        stats.put("totalComboProduct", comboProducts);

        // count extra products (productType = single, productUsage = extra)
        Long extraProducts = productRepository.countByProductTypeAndProductUsage(ProductUsageEnum.EXTRA, ProductTypeEnum.SINGLE);
        stats.put("totalExtraProduct", extraProducts);

        // count main products (productType = single, productUsage = main)
        Long mainProducts = productRepository.countByProductTypeAndProductUsage(ProductUsageEnum.MAIN, ProductTypeEnum.SINGLE);
        stats.put("totalMainProduct", mainProducts);

        // count all users (with all status)
        Long allUsers = userRepository.count();
        stats.put("totalUser", allUsers);

        // count all staff (with all status)
        Long allStaff = userRepository.countByRole(UserRoleEnum.STAFF);
        stats.put("totalStaff", allStaff);

        // count all managers (with all status)
        Long allManagers = userRepository.countByRole(UserRoleEnum.MANAGER);
        stats.put("totalManager", allManagers);

        // Top 3 products by sales (name, quantity)
        List<Object[]> top3Products = productRepository.findTop3BestSellingProduct();

        // Convert the list of Object[] to a list of Map<String, Object>
        List<Map<String, Object>> top3ProductsList = new ArrayList<>();
        for (Object[] product : top3Products) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("name", product[0]);
            productMap.put("quantity", product[1]);
            top3ProductsList.add(productMap);
        }

        stats.put("top3Products", top3ProductsList);

        // Top 3 milk tea and combo products by sales (productUsage = main)
        List<Object[]> top3MilkTeaProducts = productRepository.findTop3BestSellingProductByProductUsage(ProductUsageEnum.MAIN);
        List<Map<String, Object>> top3MilkTeaProductsList = new ArrayList<>();
        for (Object[] product : top3MilkTeaProducts) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("name", product[0]);
            productMap.put("quantity", product[1]);
            top3MilkTeaProductsList.add(productMap);
        }
        stats.put("top3MilkTeaAndComboProducts", top3MilkTeaProductsList);

        // Top 3 extra products by sales (productUsage = extra)
        List<Object[]> top3ExtraProducts = productRepository.findTop3ExtraProduct(ProductUsageEnum.EXTRA);
        List<Map<String, Object>> top3ExtraProductsList = new ArrayList<>();
        for (Object[] product : top3ExtraProducts) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("name", product[0]);
            productMap.put("quantity", product[1]);
            top3ExtraProductsList.add(productMap);
        }
        stats.put("top3ExtraProducts", top3ExtraProductsList);

        // total revenue of all paid orders
        Long result = orderRepository.totalMoneyAll();
        long total = result != null ? result : 0L;
        stats.put("totalRevenue", total);

        return stats;
    }

    public List<Map<String, Object>> getRevenueByDay() {
        List<Object[]> rawData = orderRepository.getRevenueByDay();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("orderDate", row[0]);
            item.put("totalOrders", row[1]);
            Object totalRevenueRaw = row[2];
            Long totalRevenue = totalRevenueRaw != null ? ((Number) totalRevenueRaw).longValue() : 0L;

            item.put("totalRevenue", totalRevenue);
            results.add(item);
        }
        return results;
    }

    public List<Map<String, Object>> getRevenueByWeek() {
        List<Object[]> rawData = orderRepository.getRevenueByWeek();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("week", row[0]);
            item.put("totalOrders", row[1]);
            Object totalRevenueRaw = row[2];
            Long totalRevenue = totalRevenueRaw != null ? ((Number) totalRevenueRaw).longValue() : 0L;

            item.put("totalRevenue", totalRevenue);
            results.add(item);
        }
        return results;
    }

    public List<Map<String, Object>> getRevenueByMonth() {
        List<Object[]> rawData = orderRepository.getRevenueByMonth();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", row[0]);
            item.put("totalOrders", row[1]);
            // Xử lý null cho totalRevenue
            Object totalRevenueRaw = row[2];
            Long totalRevenue = totalRevenueRaw != null ? ((Number) totalRevenueRaw).longValue() : 0L;

            item.put("totalRevenue", totalRevenue);
            results.add(item);
        }
        return results;
    }

    public List<Map<String, Object>> getRevenueByYear() {
        List<Object[]> rawData = orderRepository.getRevenueByYear();
        List<Map<String, Object>> results = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> item = new HashMap<>();
            item.put("year", row[0]);
            item.put("totalOrders", row[1]);
            // Xử lý null cho totalRevenue
            Object totalRevenueRaw = row[2];
            Long totalRevenue = totalRevenueRaw != null ? ((Number) totalRevenueRaw).longValue() : 0L;

            item.put("totalRevenue", totalRevenue);
            results.add(item);
        }
        return results;
    }
}
