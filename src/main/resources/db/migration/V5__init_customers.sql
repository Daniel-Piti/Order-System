-- CUSTOMERS TABLE
CREATE TABLE customers (
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_customers_user_id ON customers (user_id);

-- CUSTOMERS HISTORY TABLE
CREATE TABLE customers_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Trigger for INSERT
CREATE TRIGGER trg_customers_after_insert
AFTER INSERT ON customers FOR EACH ROW
INSERT INTO customers_history (id, user_id, name, phone_number, email, street_address, city, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.phone_number, NEW.email, NEW.street_address, NEW.city, NEW.created_at, NEW.updated_at);

-- Trigger for UPDATE
CREATE TRIGGER trg_customers_after_update
AFTER UPDATE ON customers FOR EACH ROW
INSERT INTO customers_history (id, user_id, name, phone_number, email, street_address, city, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.phone_number, NEW.email, NEW.street_address, NEW.city, NEW.created_at, NEW.updated_at);
