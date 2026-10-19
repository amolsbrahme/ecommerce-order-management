CREATE TABLE orders (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number   VARCHAR(64)     NOT NULL,
    customer_id    VARCHAR(64)     NOT NULL,
    status         VARCHAR(20)     NOT NULL,
    total_amount   DECIMAL(12, 2)  NOT NULL,
    failure_reason VARCHAR(512),
    created_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_orders_order_number UNIQUE (order_number)
);

CREATE TABLE order_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT          NOT NULL,
    sku         VARCHAR(64)     NOT NULL,
    quantity    INT             NOT NULL,
    unit_price  DECIMAL(12, 2)  NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT chk_order_items_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
