package com.tenacious.ecommerce.order.service;

import com.tenacious.ecommerce.order.dto.OrderItemResponse;
import com.tenacious.ecommerce.order.dto.OrderResponse;
import com.tenacious.ecommerce.order.model.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(item.getSku(), item.getQuantity(), item.getUnitPrice(),
                        item.lineTotal()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getFailureReason(),
                items,
                order.getCreatedAt()
        );
    }
}
