-- CATEGORIES TABLE
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    manager_id VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) AUTO_INCREMENT = 1000;

CREATE INDEX idx_categories_manager_id ON categories(manager_id);

-- CATEGORIES HISTORY TABLE
CREATE TABLE categories_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id BIGINT NOT NULL,
    manager_id VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- INSERT TRIGGER
CREATE TRIGGER trg_categories_after_insert
AFTER INSERT ON categories FOR EACH ROW
INSERT INTO categories_history (id, manager_id, category, created_at, updated_at)
VALUES (NEW.id, NEW.manager_id, NEW.category, NEW.created_at, NEW.updated_at);

-- UPDATE TRIGGER
CREATE TRIGGER trg_categories_after_update
AFTER UPDATE ON categories FOR EACH ROW
INSERT INTO categories_history (id, manager_id, category, created_at, updated_at)
VALUES (NEW.id, NEW.manager_id, NEW.category, NEW.created_at, NEW.updated_at);
