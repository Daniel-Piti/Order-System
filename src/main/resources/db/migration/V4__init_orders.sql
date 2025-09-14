-- ORDERS TABLE
CREATE TABLE orders (
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    location_id VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_city VARCHAR(255) NOT NULL,
    customer_address VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    products JSON,
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_orders_user_id ON orders (user_id);

-- ORDERS HISTORY TABLE
CREATE TABLE orders_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    location_id VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(20) NOT NULL,
    customer_city VARCHAR(255) NOT NULL,
    customer_address VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    products JSON,
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- INSERT TRIGGER
CREATE TRIGGER trg_orders_after_insert
AFTER INSERT ON orders FOR EACH ROW
INSERT INTO orders_history(id, user_id, location_id, customer_name, customer_phone, customer_city,
                           customer_address, status, products, total_price, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.location_id, NEW.customer_name, NEW.customer_phone, NEW.customer_city,
        NEW.customer_address, NEW.status, NEW.products, NEW.total_price, NEW.created_at, NEW.updated_at);

-- UPDATE TRIGGER
CREATE TRIGGER trg_orders_after_update
AFTER UPDATE ON orders FOR EACH ROW
INSERT INTO orders_history(id, user_id, location_id, customer_name, customer_phone, customer_city,
                           customer_address, status, products, total_price, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.location_id, NEW.customer_name, NEW.customer_phone, NEW.customer_city,
        NEW.customer_address, NEW.status, NEW.products, NEW.total_price, NEW.created_at, NEW.updated_at);
