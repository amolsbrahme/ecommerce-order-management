package com.tenacious.ecommerce.inventory.service;

import com.tenacious.ecommerce.inventory.dto.StockRequest;
import com.tenacious.ecommerce.inventory.dto.StockResponse;

public interface InventoryService {

    StockResponse create(StockRequest request);

    StockResponse getBySku(String sku);

    /** Reserves {@code quantity} units against the SKU's available stock. */
    StockResponse reserve(String sku, int quantity);

    /** Releases previously reserved units back into available stock (compensating action). */
    StockResponse release(String sku, int quantity);

    StockResponse setAvailableQuantity(String sku, int availableQuantity);
}
