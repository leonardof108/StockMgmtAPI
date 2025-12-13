package com.leonardof108.StockMgmtAPI.repository;

import com.leonardof108.StockMgmtAPI.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}