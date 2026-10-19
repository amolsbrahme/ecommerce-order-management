package com.tenacious.ecommerce.inventory.service;

import com.tenacious.ecommerce.inventory.dto.StockRequest;
import com.tenacious.ecommerce.inventory.dto.StockResponse;
import com.tenacious.ecommerce.inventory.exception.DuplicateStockException;
import com.tenacious.ecommerce.inventory.exception.InsufficientStockException;
import com.tenacious.ecommerce.inventory.exception.StockNotFoundException;
import com.tenacious.ecommerce.inventory.model.Stock;
import com.tenacious.ecommerce.inventory.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final StockRepository stockRepository;

    @Override
    public StockResponse create(StockRequest request) {
        if (stockRepository.existsBySku(request.sku())) {
            throw new DuplicateStockException(request.sku());
        }
        Stock stock = Stock.builder()
                .sku(request.sku())
                .availableQuantity(request.availableQuantity())
                .reservedQuantity(0)
                .build();
        return toResponse(stockRepository.save(stock));
    }

    @Override
    @Transactional(readOnly = true)
    public StockResponse getBySku(String sku) {
        return toResponse(findOrThrow(sku));
    }

    @Override
    @Transactional
    public StockResponse reserve(String sku, int quantity) {
        Stock stock = stockRepository.findBySkuForUpdate(sku)
                .orElseThrow(() -> new StockNotFoundException(sku));

        if (!stock.hasSufficientStock(quantity)) {
            throw new InsufficientStockException(sku, quantity, stock.getAvailableQuantity());
        }
        stock.reserve(quantity);
        return toResponse(stockRepository.save(stock));
    }

    @Override
    @Transactional
    public StockResponse release(String sku, int quantity) {
        Stock stock = stockRepository.findBySkuForUpdate(sku)
                .orElseThrow(() -> new StockNotFoundException(sku));
        stock.release(quantity);
        return toResponse(stockRepository.save(stock));
    }

    @Override
    @Transactional
    public StockResponse setAvailableQuantity(String sku, int availableQuantity) {
        Stock stock = findOrThrow(sku);
        stock.setAvailableQuantity(availableQuantity);
        return toResponse(stockRepository.save(stock));
    }

    private Stock findOrThrow(String sku) {
        return stockRepository.findBySku(sku).orElseThrow(() -> new StockNotFoundException(sku));
    }

    private StockResponse toResponse(Stock stock) {
        return new StockResponse(stock.getId(), stock.getSku(), stock.getAvailableQuantity(),
                stock.getReservedQuantity(), stock.getUpdatedAt());
    }
}
