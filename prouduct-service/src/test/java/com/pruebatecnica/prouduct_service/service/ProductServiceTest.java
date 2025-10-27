package com.pruebatecnica.prouduct_service.service;

import com.pruebatecnica.prouduct_service.dto.ProductRequest;
import com.pruebatecnica.prouduct_service.model.Product;
import com.pruebatecnica.prouduct_service.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Test
    public void updateStock_increase_and_decrease() {
        Product p = new Product();
        p.setId(1L);
        p.setName("X");
        p.setPrice(new BigDecimal("10.00"));
        p.setStock(5);
        p.setActive(true);

        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        // increase by 3 => 8
        Optional<Product> after = service.updateStock(1L, 3);
        assertTrue(after.isPresent());
        assertEquals(8, after.get().getStock());

        // decrease by 2 => 6
        after = service.updateStock(1L, -2);
        assertTrue(after.isPresent());
        assertEquals(6, after.get().getStock());
    }

    @Test
    public void updateStock_decrease_below_zero_throws() {
        Product p = new Product();
        p.setId(2L);
        p.setName("Y");
        p.setPrice(new BigDecimal("1.00"));
        p.setStock(1);
        p.setActive(true);

        when(repository.findById(2L)).thenReturn(Optional.of(p));

        assertThrows(IllegalArgumentException.class, () -> service.updateStock(2L, -5));
    }

    @Test
    public void checkAvailability_true_and_false() {
        Product p = new Product();
        p.setId(3L);
        p.setName("Z");
        p.setPrice(new BigDecimal("2.00"));
        p.setStock(4);
        p.setActive(true);

        when(repository.findById(3L)).thenReturn(Optional.of(p));

        assertTrue(service.checkAvailability(3L, 2));
        assertFalse(service.checkAvailability(3L, 10));

        // not found
        when(repository.findById(4L)).thenReturn(Optional.empty());
        assertFalse(service.checkAvailability(4L, 1));
    }
}
