package com.src.milkTea.controller;

import com.src.milkTea.service.CashDrawerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/cash-drawer")
@SecurityRequirement(name = "api")
public class CashDrawerAPI {
    @Autowired
    private CashDrawerService cashDrawerService;

    @Operation(summary = "Open cash drawer for today")
    @PostMapping("/open")
    public ResponseEntity<?> openDrawer(@RequestParam double openingBalance) {
        return ResponseEntity.ok(cashDrawerService.openDrawer(openingBalance));
    }

    @Operation(summary = "Close cash drawer for today")
    @PostMapping("/close")
    public ResponseEntity<?> closeDrawer(
        @RequestParam double actualAmount,
        @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(cashDrawerService.closeDrawer(actualAmount, note));
    }


    @Operation(summary = "Get all cash drawers")
    @GetMapping("/all")
    public ResponseEntity<?> getAllDrawers(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(cashDrawerService.getAllDrawers(pageable));
    }

    @Operation(summary = "Get current cash drawer status")
    @GetMapping("/current")
    public ResponseEntity<?> getCurrentDrawer() {
        return ResponseEntity.ok(cashDrawerService.getCurrentDrawerStatus());
    }

}
