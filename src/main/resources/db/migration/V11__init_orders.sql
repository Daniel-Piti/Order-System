-- ORDERS TABLE
CREATE TABLE orders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    order_source VARCHAR(20) NOT NULL,
    manager_id VARCHAR(255) NOT NULL,
    agent_id BIGINT NULL,
    customer_id VARCHAR(255) NULL,
    
    -- Store (pickup location) - selected by customer
    store_street_address VARCHAR(255) NULL,
    store_city VARCHAR(100) NULL,
    store_phone_number VARCHAR(20) NULL,
    
    -- Customer (Buyer) data - can be linked or standalone
    customer_name VARCHAR(255) NULL,
    customer_phone VARCHAR(20) NULL,
    customer_email VARCHAR(255) NULL,
    customer_street_address VARCHAR(255) NULL,
    customer_city VARCHAR(100) NULL,
    customer_state_id VARCHAR(20) NULL,
    
    -- Order details
    status VARCHAR(50) NOT NULL,
    products JSON NOT NULL,
    products_version INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    discount DECIMAL(10, 2) NOT NULL CHECK (discount >= 0),
    link_expires_at TIMESTAMP NOT NULL,
    notes VARCHAR(2048) NOT NULL DEFAULT '',
    placed_at TIMESTAMP NULL,
    done_at TIMESTAMP NULL,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) AUTO_INCREMENT = 10000;

CREATE INDEX idx_orders_manager_id ON orders (manager_id);
CREATE INDEX idx_orders_agent_id ON orders (agent_id);
CREATE INDEX idx_orders_order_source ON orders (order_source);
CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_products_version ON orders (products_version);
CREATE INDEX idx_orders_link_expires_at ON orders (link_expires_at);
CREATE INDEX idx_orders_manager_status ON orders (manager_id, status);
CREATE INDEX idx_orders_manager_created ON orders (manager_id, created_at);
CREATE INDEX idx_orders_manager_agent ON orders (manager_id, agent_id);

-- ORDERS HISTORY TABLE
CREATE TABLE orders_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id BIGINT NOT NULL,
    order_source VARCHAR(20) NOT NULL,
    manager_id VARCHAR(255) NOT NULL,
    agent_id BIGINT NULL,
    customer_id VARCHAR(255) NULL,
    
    -- Store location snapshot
    store_street_address VARCHAR(255) NULL,
    store_city VARCHAR(100) NULL,
    store_phone_number VARCHAR(20) NULL,
    
    -- Customer data
    customer_name VARCHAR(255) NULL,
    customer_phone VARCHAR(20) NULL,
    customer_email VARCHAR(255) NULL,
    customer_street_address VARCHAR(255) NULL,
    customer_city VARCHAR(100) NULL,
    customer_state_id VARCHAR(20) NULL,
    
    -- Order details
    status VARCHAR(50) NOT NULL,
    products JSON NOT NULL,
    products_version INT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    discount DECIMAL(10, 2) NOT NULL CHECK (discount >= 0),
    link_expires_at TIMESTAMP NOT NULL,
    notes VARCHAR(2048) NOT NULL DEFAULT '',
    placed_at TIMESTAMP NULL,
    done_at TIMESTAMP NULL,
    
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- INSERT TRIGGER
CREATE TRIGGER trg_orders_after_insert
AFTER INSERT ON orders FOR EACH ROW
INSERT INTO orders_history(
    id, order_source, manager_id, agent_id, customer_id,
    store_street_address, store_city, store_phone_number,
    customer_name, customer_phone, customer_email, 
    customer_street_address, customer_city, customer_state_id,
    status, products, products_version, total_price, discount, link_expires_at, notes, placed_at, done_at,
    created_at, updated_at
)
VALUES (
    NEW.id, NEW.order_source, NEW.manager_id, NEW.agent_id, NEW.customer_id,
    NEW.store_street_address, NEW.store_city, NEW.store_phone_number,
    NEW.customer_name, NEW.customer_phone, NEW.customer_email,
    NEW.customer_street_address, NEW.customer_city, NEW.customer_state_id,
    NEW.status, NEW.products, NEW.products_version, NEW.total_price, NEW.discount, NEW.link_expires_at, NEW.notes, NEW.placed_at, NEW.done_at,
    NEW.created_at, NEW.updated_at
);

-- UPDATE TRIGGER
CREATE TRIGGER trg_orders_after_update
AFTER UPDATE ON orders FOR EACH ROW
INSERT INTO orders_history(
    id, order_source, manager_id, agent_id, customer_id,
    store_street_address, store_city, store_phone_number,
    customer_name, customer_phone, customer_email,
    customer_street_address, customer_city, customer_state_id,
    status, products, products_version, total_price, discount, link_expires_at, notes, placed_at, done_at,
    created_at, updated_at
)
VALUES (
    NEW.id, NEW.order_source, NEW.manager_id, NEW.agent_id, NEW.customer_id,
    NEW.store_street_address, NEW.store_city, NEW.store_phone_number,
    NEW.customer_name, NEW.customer_phone, NEW.customer_email,
    NEW.customer_street_address, NEW.customer_city, NEW.customer_state_id,
    NEW.status, NEW.products, NEW.products_version, NEW.total_price, NEW.discount, NEW.link_expires_at, NEW.notes, NEW.placed_at, NEW.done_at,
    NEW.created_at, NEW.updated_at
);
