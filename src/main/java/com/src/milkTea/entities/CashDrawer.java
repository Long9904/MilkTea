package com.src.milkTea.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashDrawer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private double openingBalance;    // Số dư đầu ngày
    private double currentBalance;    // Số dư hiện tại
    private  double actualBalance;    // Số dư khi đóng két
    private boolean isOpen;
    
    @OneToMany(mappedBy = "cashDrawer")
    @JsonIgnore
    private List<Payment> payments;

    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private String note;              // Ghi chú khi đóng két
    
    @PrePersist
    public void prePersist() {
        openedAt = LocalDateTime.now();
        date = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        closedAt = LocalDateTime.now();
    }
}
