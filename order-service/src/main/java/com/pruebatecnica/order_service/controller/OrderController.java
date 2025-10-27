package com.pruebatecnica.order_service.controller;

import com.pruebatecnica.order_service.dto.OrderRequest;
import com.pruebatecnica.order_service.model.Order;
import com.pruebatecnica.order_service.model.OrderStatus;
import com.pruebatecnica.order_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.pruebatecnica.order_service.dto.ErrorResponse;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody OrderRequest req) {
        try {
            Order created = service.createOrder(req);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();

            // capture stack trace
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            String stack = sw.toString();

            // include cause message if present for quicker debugging
            String causeMsg = e.getCause() != null ? e.getCause().toString() : null;

            // Create error response with details for debugging (remove in production)
            ErrorResponse err = new ErrorResponse("Error creating order: " + e.getMessage(), causeMsg != null ? causeMsg + "\n" + stack : stack);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(err);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable Long id) {
        return service.getOrder(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Order>> list() {
        return ResponseEntity.ok(service.listOrders());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return service.updateStatus(id, status)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
