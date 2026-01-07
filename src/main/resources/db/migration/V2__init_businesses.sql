-- BUSINESSES TABLE
CREATE TABLE businesses (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    manager_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    state_id_number VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_businesses_manager_id (manager_id)
);

-- BUSINESSES HISTORY TABLE
CREATE TABLE businesses_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    manager_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    state_id_number VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Trigger for INSERT
CREATE TRIGGER trg_businesses_after_insert
AFTER INSERT ON businesses FOR EACH ROW
INSERT INTO businesses_history
    (id, manager_id, name, state_id_number, email, phone_number, street_address, city, created_at, updated_at)
VALUES (NEW.id, NEW.manager_id, NEW.name, NEW.state_id_number, NEW.email, NEW.phone_number,
        NEW.street_address, NEW.city, NEW.created_at, NEW.updated_at);

-- Trigger for UPDATE
CREATE TRIGGER trg_businesses_after_update
AFTER UPDATE ON businesses FOR EACH ROW
INSERT INTO businesses_history
    (id, manager_id, name, state_id_number, email, phone_number, street_address, city, created_at, updated_at)
VALUES (NEW.id, NEW.manager_id, NEW.name, NEW.state_id_number, NEW.email, NEW.phone_number,
        NEW.street_address, NEW.city, NEW.created_at, NEW.updated_at);

