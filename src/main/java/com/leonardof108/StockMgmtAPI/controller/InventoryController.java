package com.leonardof108.StockMgmtAPI.controller;

import com.leonardof108.StockMgmtAPI.model.Product;
import com.leonardof108.StockMgmtAPI.service.InventoryService;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Endpoint to setup initial stock (e.g., 1 iPhone)
    @PostMapping("/products")
    public Product createProduct(@RequestBody Product product) {
        return inventoryService.createProduct(product);
    }

    // Endpoint to buy
    @PostMapping("/buy/{id}")
    public ResponseEntity<String> buyProduct(@PathVariable Long id) {
        try {
            inventoryService.processPurchase(id, 1); // Buy 1 item
            return ResponseEntity.ok("Purchase Successful!");
        } catch (OptimisticLockingFailureException e) {
            // This is the specific error thrown when race condition is caught!
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Race condition detected! Please try again.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
