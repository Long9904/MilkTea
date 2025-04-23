package com.src.milkTea.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ComboDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_id")

    private Product combo; // product_type = COMBO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_product_id")
    private Product childProduct;

    private int quantity;

    private String size;
}
