package com.leonardof108.StockMgmtAPI.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    // If two users try to buy this product at the EXACT same time,
    // one will succeed, and the other will get an ObjectOptimisticLockingFailureException.
    @Version
    private Long version;
}