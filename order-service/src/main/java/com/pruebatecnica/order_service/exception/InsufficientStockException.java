package com.pruebatecnica.order_service.exception;

public class InsufficientStockException extends RuntimeException {
    private final Long productId;
    private final int requestedQuantity;
    private final int availableStock;

    public InsufficientStockException(Long productId, int requestedQuantity, int availableStock) {
        super(String.format("Insufficient stock for product %d. Requested: %d, Available: %d", 
            productId, requestedQuantity, availableStock));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public Long getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}