package com.src.milkTea.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.src.milkTea.enums.ProductStatusEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String code; // Mã khuyến mãi
    private String description;

    private double minTotal; // Điều kiện áp dụng
    private double discountPercent; // % giảm (ví dụ 10 = giảm 10%)

    private LocalDateTime dateOpen;
    private LocalDateTime dateEnd;

    @Enumerated(EnumType.STRING)
    private ProductStatusEnum status; // Active, deleted

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Payment> payments;

}
