package com.tenacious.ecommerce.order.service;

import com.tenacious.ecommerce.order.client.InventoryGateway;
import com.tenacious.ecommerce.order.client.InventoryStockResponse;
import com.tenacious.ecommerce.order.dto.OrderItemRequest;
import com.tenacious.ecommerce.order.dto.OrderRequest;
import com.tenacious.ecommerce.order.dto.OrderResponse;
import com.tenacious.ecommerce.order.event.OrderEventPublisher;
import com.tenacious.ecommerce.order.exception.InsufficientStockException;
import com.tenacious.ecommerce.order.model.Order;
import com.tenacious.ecommerce.order.model.OrderStatus;
import com.tenacious.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private InventoryGateway inventoryGateway;
    @Mock
    private OrderEventPublisher orderEventPublisher;

    private final OrderMapper orderMapper = new OrderMapper();

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, inventoryGateway, orderEventPublisher, orderMapper);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });
    }

    @Test
    void placeOrder_confirmsOrderAndPublishesEvent_whenAllItemsReserved() {
        OrderRequest request = new OrderRequest("customer-1", List.of(
                new OrderItemRequest("SKU-100", 2, new BigDecimal("10.00")),
                new OrderItemRequest("SKU-200", 1, new BigDecimal("5.00"))
        ));
        when(inventoryGateway.reserveStock(anyString(), anyInt()))
                .thenReturn(new InventoryStockResponse(1L, "SKU-X", 10, 2));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.totalAmount()).isEqualByComparingTo("25.00");
        verify(inventoryGateway, times(2)).reserveStock(anyString(), anyInt());
        verify(inventoryGateway, never()).releaseStockBestEffort(anyString(), anyInt());
        verify(orderEventPublisher).publishOrderPlaced(any());
    }

    @Test
    void placeOrder_failsAndCompensatesEarlierReservations_whenLaterItemHasInsufficientStock() {
        OrderRequest request = new OrderRequest("customer-1", List.of(
                new OrderItemRequest("SKU-100", 2, new BigDecimal("10.00")),
                new OrderItemRequest("SKU-200", 100, new BigDecimal("5.00"))
        ));
        when(inventoryGateway.reserveStock("SKU-100", 2))
                .thenReturn(new InventoryStockResponse(1L, "SKU-100", 10, 2));
        when(inventoryGateway.reserveStock("SKU-200", 100))
                .thenThrow(new InsufficientStockException("SKU-200", 100));

        OrderResponse response = orderService.placeOrder(request);

        assertThat(response.status()).isEqualTo(OrderStatus.FAILED);
        assertThat(response.failureReason()).contains("SKU-200");

        ArgumentCaptor<String> skuCaptor = ArgumentCaptor.forClass(String.class);
        verify(inventoryGateway).releaseStockBestEffort(skuCaptor.capture(), eq(2));
        assertThat(skuCaptor.getValue()).isEqualTo("SKU-100");
        verify(orderEventPublisher, never()).publishOrderPlaced(any());
    }
}
