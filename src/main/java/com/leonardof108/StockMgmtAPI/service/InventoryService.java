package com.leonardof108.StockMgmtAPI.service;

import com.leonardof108.StockMgmtAPI.model.Product;
import com.leonardof108.StockMgmtAPI.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Helper to setup data
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product processPurchase(Long productId, int quantityToBuy) throws Exception {
        // 1. Fetch the product (this also fetches the current 'version' number)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // 2. Business Logic Validation
        if (product.getQuantity() < quantityToBuy) {
            throw new RuntimeException("Not enough stock!");
        }

        // 3. Update the stock (In memory)
        product.setQuantity(product.getQuantity() - quantityToBuy);

        // 4. Save
        // Hibernate will generate SQL like:
        // UPDATE product SET quantity = 9, version = 2 WHERE id = 1 AND version = 1;
        // If another thread already changed the version to 2, this Update fails.
        return productRepository.save(product);
    }
}