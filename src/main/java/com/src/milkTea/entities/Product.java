package com.src.milkTea.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.src.milkTea.enums.ProductStatusEnum;
import com.src.milkTea.enums.ProductTypeEnum;
import com.src.milkTea.enums.ProductUsageEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private Double basePrice;

    @Column(unique = true, nullable = false)
    private String productCode;

    private String imageUrl;

    private String description;

    @Enumerated(EnumType.STRING)
    private ProductTypeEnum productType;

    @Enumerated(EnumType.STRING)
    private ProductUsageEnum productUsage;

    @Enumerated(EnumType.STRING)
    private ProductStatusEnum status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime createAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime updateAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime deleteAt;

    // Join with Category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;

    // Join with ComboDetail
    @OneToMany(mappedBy = "childProduct", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<ComboDetail> comboDetails = new ArrayList<>();

    // Join with OrderDetail
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<OrderDetail> orderDetails = new ArrayList<>();

    // Join with DefaultTopping
    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<DefaultTopping> defaultToppings = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updateAt = LocalDateTime.now();
    }
}
