-- PRODUCTS TABLE
CREATE TABLE products (
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    picture VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_products_user_id ON products (user_id);

-- PRODUCTS HISTORY TABLE
CREATE TABLE products_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    picture VARCHAR(1000),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TRIGGER trg_products_after_insert
AFTER INSERT ON products FOR EACH ROW
INSERT INTO products_history (id, user_id, name, price, picture, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.price, NEW.picture, NEW.created_at, NEW.updated_at);

CREATE TRIGGER trg_products_after_update
AFTER UPDATE ON products FOR EACH ROW
INSERT INTO products_history (id, user_id, name, price, picture, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.price, NEW.picture, NEW.created_at, NEW.updated_at);
