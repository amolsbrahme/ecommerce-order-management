package com.tenacious.ecommerce.order.service;

import com.tenacious.ecommerce.order.dto.OrderRequest;
import com.tenacious.ecommerce.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request);

    OrderResponse getByOrderNumber(String orderNumber);

    List<OrderResponse> getByCustomer(String customerId);
}
