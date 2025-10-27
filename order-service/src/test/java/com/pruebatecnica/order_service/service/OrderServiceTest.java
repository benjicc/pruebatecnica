package com.pruebatecnica.order_service.service;

import com.pruebatecnica.order_service.client.ProductClient;
import com.pruebatecnica.order_service.dto.AvailabilityResponse;
import com.pruebatecnica.order_service.dto.OrderRequest;
import com.pruebatecnica.order_service.model.Order;
import com.pruebatecnica.order_service.model.OrderItem;
import com.pruebatecnica.order_service.model.OrderStatus;
import com.pruebatecnica.order_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest.OrderItemRequest item(Long productId, int qty) {
        OrderRequest.OrderItemRequest i = new OrderRequest.OrderItemRequest();
        i.setProductId(productId);
        i.setQuantity(qty);
        return i;
    }

    @Test
    public void createOrder_success_reservesStockAndSavesOrder() {
        OrderRequest req = new OrderRequest();
        req.setCustomerName("Test");
        req.setCustomerEmail("t@t.com");
        req.setItems(Arrays.asList(item(1L, 2)));

        // Prepare product returned by ProductClient
        ProductClient.Product p = new ProductClient.Product();
        p.setId(1L);
        p.setPrice(new BigDecimal("10.00"));

        when(productClient.getProduct(1L)).thenReturn(p);
    AvailabilityResponse avail = new AvailabilityResponse();
    avail.setAvailable(true);
    avail.setStock(5);
    when(productClient.checkAvailability(1L, 2)).thenReturn(avail);
        // decreaseStock shouldn't throw
        doNothing().when(productClient).decreaseStock(1L, 2);

        when(repository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.createOrder(req);

        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        assertEquals(1, result.getItems().size());
        OrderItem it = result.getItems().get(0);
        assertEquals(0, it.getUnitPrice().compareTo(new BigDecimal("10.00")));
        assertEquals(new BigDecimal("20.00"), result.getTotalAmount());

        verify(productClient).decreaseStock(1L, 2);
        verify(repository).save(any(Order.class));
    }

    @Test
    public void createOrder_whenDecreaseFails_rollsBackPreviouslyReserved() {
        // Two items: first will be reserved, second will fail during decreaseStock
        OrderRequest req = new OrderRequest();
        req.setCustomerName("T");
        req.setCustomerEmail("t@t.com");
        req.setItems(Arrays.asList(item(1L, 1), item(2L, 1)));

        ProductClient.Product p1 = new ProductClient.Product();
        p1.setId(1L);
        p1.setPrice(new BigDecimal("5.00"));

        ProductClient.Product p2 = new ProductClient.Product();
        p2.setId(2L);
        p2.setPrice(new BigDecimal("2.00"));

        when(productClient.getProduct(1L)).thenReturn(p1);
        when(productClient.getProduct(2L)).thenReturn(p2);
    AvailabilityResponse aTrue = new AvailabilityResponse();
    aTrue.setAvailable(true);
    aTrue.setStock(10);
    when(productClient.checkAvailability(eq(1L), anyInt())).thenReturn(aTrue);
    when(productClient.checkAvailability(eq(2L), anyInt())).thenReturn(aTrue);

        // first decrease ok, second throws
        doNothing().when(productClient).decreaseStock(1L, 1);
        doThrow(new RuntimeException("downstream error")).when(productClient).decreaseStock(2L, 1);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> orderService.createOrder(req));
        assertTrue(ex.getMessage().contains("Failed to reserve product stock"));

        // verify rollback: increaseStock called for first reserved item
        verify(productClient).increaseStock(1L, 1);
        verify(repository, never()).save(any());
    }

    @Test
    public void createOrder_whenNotAvailable_throwsInsufficientStock() {
        OrderRequest req = new OrderRequest();
        req.setCustomerName("T");
        req.setCustomerEmail("t@t.com");
        req.setItems(Arrays.asList(item(3L, 5)));

        ProductClient.Product p3 = new ProductClient.Product();
        p3.setId(3L);
        p3.setPrice(new BigDecimal("1.00"));

        when(productClient.getProduct(3L)).thenReturn(p3);
    AvailabilityResponse aFalse = new AvailabilityResponse();
    aFalse.setAvailable(false);
    aFalse.setStock(2);
    when(productClient.checkAvailability(3L, 5)).thenReturn(aFalse);

        assertThrows(RuntimeException.class, () -> orderService.createOrder(req));
        verify(repository, never()).save(any());
    }
}
