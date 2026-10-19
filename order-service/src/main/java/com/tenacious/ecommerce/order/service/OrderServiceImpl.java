package com.tenacious.ecommerce.order.service;

import com.tenacious.ecommerce.order.client.InventoryGateway;
import com.tenacious.ecommerce.order.dto.OrderItemRequest;
import com.tenacious.ecommerce.order.dto.OrderRequest;
import com.tenacious.ecommerce.order.dto.OrderResponse;
import com.tenacious.ecommerce.order.event.OrderEventPublisher;
import com.tenacious.ecommerce.order.event.OrderPlacedEvent;
import com.tenacious.ecommerce.order.exception.InsufficientStockException;
import com.tenacious.ecommerce.order.exception.InventoryServiceUnavailableException;
import com.tenacious.ecommerce.order.exception.OrderNotFoundException;
import com.tenacious.ecommerce.order.model.Order;
import com.tenacious.ecommerce.order.model.OrderItem;
import com.tenacious.ecommerce.order.model.OrderStatus;
import com.tenacious.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Orchestrates order placement: reserve stock for every line item via the
 * Inventory Service (synchronous, resilience-wrapped), persist the order,
 * and - only on full success - publish the order-placed event for the
 * Notification Service to pick up asynchronously.
 *
 * <p>This is intentionally a simple, order-scoped compensation ("saga-lite")
 * rather than a full saga orchestrator: if item N fails to reserve, items
 * 1..N-1 that were already reserved in this same request are released
 * again before the order is persisted with status {@code FAILED}. That is
 * an appropriate, explainable scope for a capstone project; a production
 * system with many more downstream steps would likely use a dedicated
 * saga/orchestration mechanism instead.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryGateway inventoryGateway;
    private final OrderEventPublisher orderEventPublisher;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        String orderNumber = "ORD-" + UUID.randomUUID();
        List<String> reservedSkus = new ArrayList<>();
        List<OrderItemRequest> reservedItems = new ArrayList<>();

        try {
            for (OrderItemRequest item : request.items()) {
                inventoryGateway.reserveStock(item.sku(), item.quantity());
                reservedSkus.add(item.sku());
                reservedItems.add(item);
            }

            Order order = buildOrder(orderNumber, request, OrderStatus.CONFIRMED, null);
            Order saved = orderRepository.save(order);

            orderEventPublisher.publishOrderPlaced(toEvent(saved));

            return orderMapper.toResponse(saved);

        } catch (InsufficientStockException | InventoryServiceUnavailableException ex) {
            log.warn("Order {} failed during stock reservation: {}", orderNumber, ex.getMessage());
            compensate(reservedSkus, reservedItems);

            Order failedOrder = buildOrder(orderNumber, request, OrderStatus.FAILED, ex.getMessage());
            Order saved = orderRepository.save(failedOrder);
            return orderMapper.toResponse(saved);
        }
    }

    private void compensate(List<String> reservedSkus, List<OrderItemRequest> reservedItems) {
        for (int i = 0; i < reservedSkus.size(); i++) {
            inventoryGateway.releaseStockBestEffort(reservedSkus.get(i), reservedItems.get(i).quantity());
        }
    }

    private Order buildOrder(String orderNumber, OrderRequest request, OrderStatus status, String failureReason) {
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerId(request.customerId())
                .status(status)
                .totalAmount(BigDecimal.ZERO)
                .failureReason(failureReason)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            OrderItem item = OrderItem.builder()
                    .sku(itemRequest.sku())
                    .quantity(itemRequest.quantity())
                    .unitPrice(itemRequest.unitPrice())
                    .build();
            order.addItem(item);
            total = total.add(item.lineTotal());
        }
        order.setTotalAmount(total);
        return order;
    }

    private OrderPlacedEvent toEvent(Order order) {
        List<OrderPlacedEvent.OrderedItem> items = order.getItems().stream()
                .map(i -> new OrderPlacedEvent.OrderedItem(i.getSku(), i.getQuantity(), i.getUnitPrice()))
                .toList();
        return new OrderPlacedEvent(order.getOrderNumber(), order.getCustomerId(), order.getTotalAmount(),
                items, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }
}
