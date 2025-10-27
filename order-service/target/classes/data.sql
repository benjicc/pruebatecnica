-- Insertar órdenes de ejemplo
INSERT INTO ORDERS (customer_name, customer_email, order_date, status, total_amount) VALUES
('Juan Pérez', 'juan.perez@email.com', CURRENT_TIMESTAMP(), 'CONFIRMED', 1389.98),
('María López', 'maria.lopez@email.com', CURRENT_TIMESTAMP(), 'PENDING', 299.99),
('Carlos Ruiz', 'carlos.ruiz@email.com', CURRENT_TIMESTAMP(), 'CANCELLED', 2129.98);

-- Insertar items de las órdenes
-- Orden 1: Juan Pérez (Laptop + Auriculares)
INSERT INTO ORDER_ITEMS (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 1299.99),  -- 1 Laptop Dell XPS
(1, 5, 1, 89.99);    -- 1 Auriculares

-- Orden 2: María López (Monitor)
INSERT INTO ORDER_ITEMS (order_id, product_id, quantity, unit_price) VALUES
(2, 2, 1, 299.99);   -- 1 Monitor LG

-- Orden 3: Carlos Ruiz (Gaming Setup - Cancelado)
INSERT INTO ORDER_ITEMS (order_id, product_id, quantity, unit_price) VALUES
(3, 6, 1, 1999.99),  -- 1 Laptop Gaming
(3, 3, 1, 89.99),    -- 1 Teclado
(3, 4, 1, 49.99);    -- 1 Mouse