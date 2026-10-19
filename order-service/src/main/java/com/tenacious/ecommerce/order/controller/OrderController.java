package com.tenacious.ecommerce.order.controller;

import com.tenacious.ecommerce.order.dto.OrderRequest;
import com.tenacious.ecommerce.order.dto.OrderResponse;
import com.tenacious.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Order placement and retrieval")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a new order (reserves stock synchronously, then publishes an order-placed event)")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderNumber}")
    @Operation(summary = "Get an order by its order number")
    public ResponseEntity<OrderResponse> getByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getByOrderNumber(orderNumber));
    }

    @GetMapping
    @Operation(summary = "List orders for a customer")
    public ResponseEntity<List<OrderResponse>> getByCustomer(@RequestParam String customerId) {
        return ResponseEntity.ok(orderService.getByCustomer(customerId));
    }
}
