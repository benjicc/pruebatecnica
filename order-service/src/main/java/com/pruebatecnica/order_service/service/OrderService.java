package com.pruebatecnica.order_service.service;

import com.pruebatecnica.order_service.client.ProductClient;
import com.pruebatecnica.order_service.dto.AvailabilityResponse;
import com.pruebatecnica.order_service.dto.OrderRequest;
import com.pruebatecnica.order_service.exception.InsufficientStockException;
import com.pruebatecnica.order_service.model.Order;
import com.pruebatecnica.order_service.model.OrderItem;
import com.pruebatecnica.order_service.model.OrderStatus;
import com.pruebatecnica.order_service.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final ProductClient productClient;

    public OrderService(OrderRepository repository, ProductClient productClient) {
        this.repository = repository;
        this.productClient = productClient;
    }

    @Transactional
    public Order createOrder(OrderRequest req) {
        // Create order with customer info but don't persist yet
        Order order = new Order(req.getCustomerName(), req.getCustomerEmail());

        // First, fetch all products and validate availability
        Map<Long, ProductClient.Product> products = new HashMap<>();
        for (OrderRequest.OrderItemRequest itemRequest : req.getItems()) {
            // Get product details including current price
            ProductClient.Product product = productClient.getProduct(itemRequest.getProductId());
            products.put(itemRequest.getProductId(), product);

            // Check availability
            AvailabilityResponse availability = productClient.checkAvailability(
                itemRequest.getProductId(),
                itemRequest.getQuantity()
            );
            if (availability.getStock() == 0) {
                throw new InsufficientStockException(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity(),
                    0
                );
            }
            if (!availability.isAvailable()) {
                throw new InsufficientStockException(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity(),
                    availability.getStock()
                );
            }
        }

        // Reserve stock before saving the order. Keep track of reserved items to rollback if needed.
        java.util.List<OrderItem> reserved = new java.util.ArrayList<>();
        try {
            for (OrderRequest.OrderItemRequest itemRequest : req.getItems()) {
                ProductClient.Product product = products.get(itemRequest.getProductId());
                
                // decrease stock in product service
                productClient.decreaseStock(itemRequest.getProductId(), itemRequest.getQuantity());

                // create order item with product's current price and keep in reserved list
                OrderItem item = new OrderItem(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity(),
                    product.getPrice() // Use product's current price
                );
                reserved.add(item);
            }
        } catch (Exception e) {
            // rollback any successful reservations
            for (OrderItem r : reserved) {
                try {
                    // on rollback we should increase stock back (undo reservation)
                    productClient.increaseStock(r.getProductId(), r.getQuantity());
                } catch (Exception ex) {
                    // log and continue; we tried to rollback but can't do much more here
                    ex.printStackTrace();
                }
            }
            throw new RuntimeException("Failed to reserve product stock; no order created", e);
        }

        // All stock reserved successfully; attach items and persist order
        for (OrderItem r : reserved) {
            order.addItem(r);
        }
        order.setStatus(OrderStatus.CONFIRMED);
        return repository.save(order);
    }

    public Optional<Order> getOrder(Long id) {
        return repository.findById(id);
    }

    public List<Order> listOrders() {
        return repository.findAll();
    }

    @Transactional
    public Optional<Order> updateStatus(Long id, OrderStatus status) {
        return repository.findById(id).map(o -> {
            o.setStatus(status);
            return repository.save(o);
        });
    }
}
