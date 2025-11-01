-- CATEGORIES TABLE
CREATE TABLE categories (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_categories_user_category UNIQUE (user_id, category)
);

CREATE INDEX idx_categories_user_id ON categories(user_id);

-- CATEGORIES HISTORY TABLE
CREATE TABLE categories_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- INSERT TRIGGER
CREATE TRIGGER trg_categories_after_insert
AFTER INSERT ON categories FOR EACH ROW
INSERT INTO categories_history (id, user_id, category, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.category, NEW.created_at, NEW.updated_at);

-- UPDATE TRIGGER
CREATE TRIGGER trg_categories_after_update
AFTER UPDATE ON categories FOR EACH ROW
INSERT INTO categories_history (id, user_id, category, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.category, NEW.created_at, NEW.updated_at);
