package com.pruebatecnica.order_service.dto;

public class AvailabilityResponse {
    private boolean available;
    private int stock;

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }
}