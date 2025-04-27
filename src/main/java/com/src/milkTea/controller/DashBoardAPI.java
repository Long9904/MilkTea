package com.src.milkTea.controller;

import com.src.milkTea.service.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@SecurityRequirement(name = "api")
public class DashBoardAPI {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getDashBoardStats() {
        Map<String, Object> stats = dashboardService.getDashBoardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<?> getRevenueByDay() {
        return ResponseEntity.ok(dashboardService.getRevenueByDay());
    }

    @GetMapping("/revenue/weekly")
    public ResponseEntity<?> getRevenueByWeek() {
        return ResponseEntity.ok(dashboardService.getRevenueByWeek());
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<?> getRevenueByMonth() {
        return ResponseEntity.ok(dashboardService.getRevenueByMonth());
    }

    @GetMapping("/revenue/yearly")
    public ResponseEntity<?> getRevenueByYear() {
        return ResponseEntity.ok(dashboardService.getRevenueByYear());
    }
}
