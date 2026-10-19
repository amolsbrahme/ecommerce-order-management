package com.tenacious.ecommerce.inventory.service;

import com.tenacious.ecommerce.inventory.dto.StockResponse;
import com.tenacious.ecommerce.inventory.exception.InsufficientStockException;
import com.tenacious.ecommerce.inventory.exception.StockNotFoundException;
import com.tenacious.ecommerce.inventory.model.Stock;
import com.tenacious.ecommerce.inventory.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(stockRepository);
    }

    @Test
    void reserve_reducesAvailableAndIncreasesReserved_whenEnoughStock() {
        Stock stock = Stock.builder().id(1L).sku("SKU-100").availableQuantity(10).reservedQuantity(0).build();
        when(stockRepository.findBySkuForUpdate("SKU-100")).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        StockResponse response = inventoryService.reserve("SKU-100", 4);

        assertThat(response.availableQuantity()).isEqualTo(6);
        assertThat(response.reservedQuantity()).isEqualTo(4);
    }

    @Test
    void reserve_throwsInsufficientStock_whenNotEnoughAvailable() {
        Stock stock = Stock.builder().id(1L).sku("SKU-100").availableQuantity(2).reservedQuantity(0).build();
        when(stockRepository.findBySkuForUpdate("SKU-100")).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> inventoryService.reserve("SKU-100", 5))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("SKU-100");
    }

    @Test
    void reserve_throwsNotFound_whenSkuUnknown() {
        when(stockRepository.findBySkuForUpdate("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.reserve("UNKNOWN", 1))
                .isInstanceOf(StockNotFoundException.class);
    }

    @Test
    void release_movesQuantityBackFromReservedToAvailable() {
        Stock stock = Stock.builder().id(1L).sku("SKU-100").availableQuantity(6).reservedQuantity(4).build();
        when(stockRepository.findBySkuForUpdate("SKU-100")).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        StockResponse response = inventoryService.release("SKU-100", 4);

        assertThat(response.availableQuantity()).isEqualTo(10);
        assertThat(response.reservedQuantity()).isEqualTo(0);
    }
}
