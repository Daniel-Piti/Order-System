-- PRODUCTS TABLE
CREATE TABLE products (
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category_id VARCHAR(255),
    original_price DECIMAL(10, 2) NOT NULL CHECK (original_price >= 0),
    special_price DECIMAL(10, 2) NOT NULL CHECK (special_price >= 0),
    picture_url VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_products_user_id ON products (user_id);
CREATE INDEX idx_products_category_id ON products (category_id);

-- PRODUCTS HISTORY TABLE
CREATE TABLE products_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category_id VARCHAR(255),
    original_price DECIMAL(10, 2) NOT NULL CHECK (original_price >= 0),
    special_price DECIMAL(10, 2) NOT NULL CHECK (special_price >= 0),
    picture_url VARCHAR(1024) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- TRIGGERS
CREATE TRIGGER trg_products_after_insert
AFTER INSERT ON products FOR EACH ROW
INSERT INTO products_history (id, user_id, name, category_id, original_price, special_price, picture_url, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.category_id, NEW.original_price, NEW.special_price, NEW.picture_url, NEW.created_at, NEW.updated_at);

CREATE TRIGGER trg_products_after_update
AFTER UPDATE ON products FOR EACH ROW
INSERT INTO products_history (id, user_id, name, category_id, original_price, special_price, picture_url, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.category_id, NEW.original_price, NEW.special_price, NEW.picture_url, NEW.created_at, NEW.updated_at);
