package com.pruebatecnica.order_service.client;

import com.pruebatecnica.order_service.config.ProductServiceProperties;
import com.pruebatecnica.order_service.dto.AvailabilityResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;
    private final ProductServiceProperties properties;

    public static class Product {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer stock;
        private Boolean active;

        // getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
        public Integer getStock() { return stock; }
        public Boolean getActive() { return active; }

        // setters
        public void setId(Long id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public void setStock(Integer stock) { this.stock = stock; }
        public void setActive(Boolean active) { this.active = active; }
    }

    public ProductClient(RestTemplate restTemplate, ProductServiceProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public Product getProduct(Long productId) {
        try {
            String url = String.format("%s/products/%d", properties.getUrl(), productId);
            ResponseEntity<Product> resp = restTemplate.getForEntity(url, Product.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return resp.getBody();
            }
            throw new RuntimeException("Product not found");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get product details", e);
        }
    }

    /**
     * Check availability of a product for a requested quantity.
     * Expects the product service to expose an endpoint like
     * GET {productServiceUrl}/products/{id}/availability?quantity={q}
     * which returns JSON: {"available": true, "stock": 10}
     */
    public AvailabilityResponse checkAvailability(Long productId, int quantity) {
        try {
            String url = String.format("%s/products/%d/availability?quantity=%d", properties.getUrl(), productId, quantity);
            ResponseEntity<AvailabilityResponse> resp = restTemplate.getForEntity(url, AvailabilityResponse.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return resp.getBody();
            }
            throw new RuntimeException("Failed to check product availability");
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            throw new RuntimeException("Failed to check product availability", e);
        }
    }

    public void decreaseStock(Long productId, int quantity) {
        try {
            // product-service expects a signed quantity (positive to increase, negative to decrease)
            String url = String.format("%s/products/%d/stock?quantity=%d", properties.getUrl(), productId, -quantity);
            // Use patchForObject which is simpler and more compatible for PATCH requests
            restTemplate.patchForObject(url, null, Void.class);
        } catch (Exception e) {
            // Log the error for debugging
            e.printStackTrace();
            throw new RuntimeException("Failed to decrease product stock", e);
        }
    }

    public void increaseStock(Long productId, int quantity) {
        try {
            String url = String.format("%s/products/%d/stock?quantity=%d", properties.getUrl(), productId, quantity);
            restTemplate.patchForObject(url, null, Void.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to increase product stock", e);
        }
    }
}
