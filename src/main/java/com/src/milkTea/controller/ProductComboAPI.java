package com.src.milkTea.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product-combos")
@SecurityRequirement(name = "api")
public class ProductComboAPI {

}
