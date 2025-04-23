package com.src.milkTea.entities;

import jakarta.persistence.*;

@Entity
public class ComboDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "combo_id")
    private Product combo; // product_type = COMBO

    @ManyToOne
    @JoinColumn(name = "child_product_id")
    private Product childProduct;

    private int quantity;

    private String size;
}
