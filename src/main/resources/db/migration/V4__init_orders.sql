-- ORDERS TABLE
CREATE TABLE orders (
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    location_id VARCHAR(255) NOT NULL,
    link_token VARCHAR(255) NOT NULL UNIQUE,
    customer_name VARCHAR(255),
    customer_phone VARCHAR(20),
    customer_address VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    data JSON NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_link_token ON orders (link_token);

-- ORDERS HISTORY TABLE
CREATE TABLE orders_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    location_id VARCHAR(255) NOT NULL,
    link_token VARCHAR(255),
    customer_name VARCHAR(255),
    customer_phone VARCHAR(20),
    customer_address VARCHAR(255),
    status VARCHAR(50),
    data JSON,
    total_price DECIMAL(10, 2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TRIGGER trg_orders_after_insert
AFTER INSERT ON orders FOR EACH ROW
INSERT INTO orders_history(id, user_id, location_id, link_token, customer_name, customer_phone,
                           customer_address, status, data, total_price, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.location_id, NEW.link_token, NEW.customer_name, NEW.customer_phone,
        NEW.customer_address, NEW.status, NEW.data, NEW.total_price, NEW.created_at, NEW.updated_at);

CREATE TRIGGER trg_orders_after_update
AFTER UPDATE ON orders FOR EACH ROW
INSERT INTO orders_history (id, user_id, location_id, link_token, customer_name, customer_phone,
                            customer_address, status, data, total_price, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.location_id, NEW.link_token, NEW.customer_name, NEW.customer_phone,
        NEW.customer_address, NEW.status, NEW.data, NEW.total_price, NEW.created_at, NEW.updated_at);
