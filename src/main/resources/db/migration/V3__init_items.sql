-- ITEMS TABLE
CREATE TABLE items (
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    picture VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_items_user_id ON items (user_id);

-- ITEMS HISTORY TABLE
CREATE TABLE items_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    price DECIMAL(10, 2),
    picture VARCHAR(1000),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TRIGGER trg_items_after_insert
AFTER INSERT ON items FOR EACH ROW
INSERT INTO items_history (id, user_id, name, price, picture, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.price, NEW.picture, NEW.created_at, NEW.updated_at);

CREATE TRIGGER trg_items_after_update
AFTER UPDATE ON items FOR EACH ROW
INSERT INTO items_history (id, user_id, name, price, picture, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.price, NEW.picture, NEW.created_at, NEW.updated_at);
