package com.src.milkTea.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.src.milkTea.enums.ProducSizeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;

    private double unitPrice;

    @Enumerated(EnumType.STRING)
    private ProducSizeEnum size;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime updateAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime deleteAt;

    // Join with OrderMaster
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_master_id", referencedColumnName = "id")
    private OrderMaster orderMaster;

    // Join with Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private Product product;

    @PrePersist
    public void prePersist() {
        createAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updateAt = LocalDateTime.now();
    }

}
