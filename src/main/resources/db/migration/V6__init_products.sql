-- PRODUCTS TABLE
CREATE TABLE products (
    id VARCHAR(255) NOT NULL,
    manager_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    brand_id BIGINT,
    category_id BIGINT,
    minimum_price DECIMAL(10, 2) NOT NULL CHECK (minimum_price >= 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_products_manager_id ON products (manager_id);
CREATE INDEX idx_products_brand_id ON products (brand_id);
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_manager_category ON products (manager_id, category_id);
CREATE INDEX idx_products_manager_created ON products (manager_id, created_at);

-- PRODUCTS HISTORY TABLE
CREATE TABLE products_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    manager_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    brand_id BIGINT,
    category_id BIGINT,
    minimum_price DECIMAL(10, 2) NOT NULL CHECK (minimum_price >= 0),
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    description TEXT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- TRIGGERS
CREATE TRIGGER trg_products_after_insert
AFTER INSERT ON products FOR EACH ROW
INSERT INTO products_history (id, manager_id, name, brand_id, category_id, minimum_price, price, description, created_at, updated_at)
VALUES (NEW.id, NEW.manager_id, NEW.name, NEW.brand_id, NEW.category_id, NEW.minimum_price, NEW.price, NEW.description, NEW.created_at, NEW.updated_at);

CREATE TRIGGER trg_products_after_update
AFTER UPDATE ON products FOR EACH ROW
INSERT INTO products_history (id, manager_id, name, brand_id, category_id, minimum_price, price, description, created_at, updated_at)
VALUES (NEW.id, NEW.manager_id, NEW.name, NEW.brand_id, NEW.category_id, NEW.minimum_price, NEW.price, NEW.description, NEW.created_at, NEW.updated_at);
