//package com.src.milkTea.entities;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.src.milkTea.enums.OrderStatusEnum;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Entity
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class Orders {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private double totalPrice;
//
//    private String note;
//
//    private OrderStatusEnum status;
//
//    private LocalDateTime createAt;
//
//    private LocalDateTime updateAt;
//
//    private LocalDateTime deleteAt;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", referencedColumnName = "id")
//    @JsonIgnore
//    private User user;
//
//    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @JsonIgnore
//    private List<OrderDetail> orderDetails;
//
//    @PrePersist
//    public void prePersist() {
//        createAt = LocalDateTime.now();
//    }
//
//    @PreUpdate
//    public void preUpdate() {
//        updateAt = LocalDateTime.now();
//    }
//}
