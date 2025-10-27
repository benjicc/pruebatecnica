package com.pruebatecnica.order_service.repository;

import com.pruebatecnica.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
