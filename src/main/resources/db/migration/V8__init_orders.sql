-- ORDERS TABLE
CREATE TABLE orders (
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    
    -- User (Seller) pickup location - selected by customer
    user_street_address VARCHAR(255) NULL,
    user_city VARCHAR(100) NULL,
    user_phone_number VARCHAR(20) NULL,
    
    -- Customer (Buyer) data - can be linked or standalone
    customer_id VARCHAR(255) NULL,
    customer_name VARCHAR(255) NULL,
    customer_phone VARCHAR(20) NULL,
    customer_email VARCHAR(255) NULL,
    customer_street_address VARCHAR(255) NULL,
    customer_city VARCHAR(100) NULL,
    
    -- Order details
    status VARCHAR(50) NOT NULL,
    products JSON NOT NULL,
    products_version INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    delivery_date DATE NULL,
    link_expires_at TIMESTAMP NOT NULL,
    notes VARCHAR(2048) NOT NULL DEFAULT '',
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_products_version ON orders (products_version);
CREATE INDEX idx_orders_link_expires_at ON orders (link_expires_at);
CREATE INDEX idx_orders_user_status ON orders (user_id, status);
CREATE INDEX idx_orders_user_created ON orders (user_id, created_at);

-- ORDERS HISTORY TABLE
CREATE TABLE orders_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    
    -- User location snapshot
    user_street_address VARCHAR(255) NULL,
    user_city VARCHAR(100) NULL,
    user_phone_number VARCHAR(20) NULL,
    
    -- Customer data
    customer_id VARCHAR(255) NULL,
    customer_name VARCHAR(255) NULL,
    customer_phone VARCHAR(20) NULL,
    customer_email VARCHAR(255) NULL,
    customer_street_address VARCHAR(255) NULL,
    customer_city VARCHAR(100) NULL,
    
    -- Order details
    status VARCHAR(50) NOT NULL,
    products JSON NOT NULL,
    products_version INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    delivery_date DATE NULL,
    link_expires_at TIMESTAMP NOT NULL,
    notes VARCHAR(2048) NOT NULL DEFAULT '',
    
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- INSERT TRIGGER
CREATE TRIGGER trg_orders_after_insert
AFTER INSERT ON orders FOR EACH ROW
INSERT INTO orders_history(
    id, user_id, 
    user_street_address, user_city, user_phone_number,
    customer_id, customer_name, customer_phone, customer_email, 
    customer_street_address, customer_city,
    status, products, products_version, total_price, delivery_date, link_expires_at,
    notes,
    created_at, updated_at
)
VALUES (
    NEW.id, NEW.user_id,
    NEW.user_street_address, NEW.user_city, NEW.user_phone_number,
    NEW.customer_id, NEW.customer_name, NEW.customer_phone, NEW.customer_email,
    NEW.customer_street_address, NEW.customer_city,
    NEW.status, NEW.products, NEW.products_version, NEW.total_price, NEW.delivery_date, NEW.link_expires_at,
    NEW.notes,
    NEW.created_at, NEW.updated_at
);

-- UPDATE TRIGGER
CREATE TRIGGER trg_orders_after_update
AFTER UPDATE ON orders FOR EACH ROW
INSERT INTO orders_history(
    id, user_id,
    user_street_address, user_city, user_phone_number,
    customer_id, customer_name, customer_phone, customer_email,
    customer_street_address, customer_city,
    status, products, products_version, total_price, delivery_date, link_expires_at,
    notes,
    created_at, updated_at
)
VALUES (
    NEW.id, NEW.user_id,
    NEW.user_street_address, NEW.user_city, NEW.user_phone_number,
    NEW.customer_id, NEW.customer_name, NEW.customer_phone, NEW.customer_email,
    NEW.customer_street_address, NEW.customer_city,
    NEW.status, NEW.products, NEW.products_version, NEW.total_price, NEW.delivery_date, NEW.link_expires_at,
    NEW.notes,
    NEW.created_at, NEW.updated_at
);
