package com.tenacious.ecommerce.inventory.controller;

import com.tenacious.ecommerce.inventory.dto.StockAdjustmentRequest;
import com.tenacious.ecommerce.inventory.dto.StockRequest;
import com.tenacious.ecommerce.inventory.dto.StockResponse;
import com.tenacious.ecommerce.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock level tracking and reservation")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @Operation(summary = "Create a stock record for a new SKU")
    public ResponseEntity<StockResponse> create(@Valid @RequestBody StockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.create(request));
    }

    @GetMapping("/{sku}")
    @Operation(summary = "Get current stock levels for a SKU")
    public ResponseEntity<StockResponse> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(inventoryService.getBySku(sku));
    }

    @PostMapping("/{sku}/reserve")
    @Operation(summary = "Reserve stock for an order (called by Order Service)")
    public ResponseEntity<StockResponse> reserve(@PathVariable String sku,
                                                   @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.reserve(sku, request.quantity()));
    }

    @PostMapping("/{sku}/release")
    @Operation(summary = "Release previously reserved stock (compensating action)")
    public ResponseEntity<StockResponse> release(@PathVariable String sku,
                                                   @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.release(sku, request.quantity()));
    }

    @PutMapping("/{sku}")
    @Operation(summary = "Set the available quantity for a SKU (admin/replenishment)")
    public ResponseEntity<StockResponse> setAvailableQuantity(@PathVariable String sku,
                                                                @Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(inventoryService.setAvailableQuantity(sku, request.quantity()));
    }
}
