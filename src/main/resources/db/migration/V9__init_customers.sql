-- CUSTOMERS TABLE
CREATE TABLE customers (
    id VARCHAR(255) NOT NULL,
    agent_id BIGINT,
    manager_id VARCHAR(255) NOT NULL,
    discount_percentage INT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state_id VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_customers_agent_id ON customers (agent_id);
CREATE INDEX idx_customers_manager_id ON customers (manager_id);

-- CUSTOMERS HISTORY TABLE
CREATE TABLE customers_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    agent_id BIGINT,
    manager_id VARCHAR(255) NOT NULL,
    discount_percentage INT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(255) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state_id VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Trigger for INSERT
CREATE TRIGGER trg_customers_after_insert
AFTER INSERT ON customers FOR EACH ROW
INSERT INTO customers_history (id, agent_id, manager_id, discount_percentage, name, phone_number, email, street_address, city, state_id, created_at, updated_at)
VALUES (NEW.id, NEW.agent_id, NEW.manager_id, NEW.discount_percentage, NEW.name, NEW.phone_number, NEW.email, NEW.street_address, NEW.city, NEW.state_id, NEW.created_at, NEW.updated_at);

-- Trigger for UPDATE
CREATE TRIGGER trg_customers_after_update
AFTER UPDATE ON customers FOR EACH ROW
INSERT INTO customers_history (id, agent_id, manager_id, discount_percentage, name, phone_number, email, street_address, city, state_id, created_at, updated_at)
VALUES (NEW.id, NEW.agent_id, NEW.manager_id, NEW.discount_percentage, NEW.name, NEW.phone_number, NEW.email, NEW.street_address, NEW.city, NEW.state_id, NEW.created_at, NEW.updated_at);
