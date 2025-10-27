package com.pruebatecnica.prouduct_service.service;

import com.pruebatecnica.prouduct_service.dto.ProductRequest;
import com.pruebatecnica.prouduct_service.model.Product;
import com.pruebatecnica.prouduct_service.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        Product product = new Product(
            request.getName(),
            request.getPrice(),
            request.getStock(),
            request.getActive()
        );
        return repository.save(product);
    }

    public Optional<Product> getProduct(Long id) {
        return repository.findById(id);
    }

    public List<Product> listProducts() {
        return repository.findAll();
    }

    @Transactional
    public Optional<Product> updateProduct(Long id, ProductRequest request) {
        return repository.findById(id)
            .map(product -> {
                product.setName(request.getName());
                product.setPrice(request.getPrice());
                product.setStock(request.getStock());
                product.setActive(request.getActive());
                return repository.save(product);
            });
    }

    @Transactional
    public Optional<Product> updateStock(Long id, Integer quantity) {
        return repository.findById(id)
            .map(product -> {
                // Log for debugging the incoming quantity
                System.out.println("[ProductService] updateStock id=" + id + " quantity=" + quantity + " current=" + product.getStock());
                int newStock = product.getStock() + quantity;
                if (newStock < 0) {
                    throw new IllegalArgumentException("Not enough stock available");
                }
                product.setStock(newStock);
                return repository.save(product);
            });
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean checkAvailability(Long id, Integer quantity) {
        return repository.findById(id)
            .map(product -> product.getActive() && product.getStock() >= quantity)
            .orElse(false);
    }
}