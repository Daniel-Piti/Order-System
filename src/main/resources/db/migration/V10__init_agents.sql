-- AGENTS TABLE
CREATE TABLE agents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    manager_id VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) AUTO_INCREMENT = 1000;

CREATE INDEX idx_agents_manager_id ON agents(manager_id);

-- AGENTS HISTORY TABLE
CREATE TABLE agents_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id BIGINT NOT NULL,
    manager_id VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- INSERT TRIGGER
CREATE TRIGGER trg_agents_after_insert
AFTER INSERT ON agents FOR EACH ROW
INSERT INTO agents_history (
    id,
    manager_id,
    first_name,
    last_name,
    email,
    password,
    phone_number,
    street_address,
    city,
    created_at,
    updated_at
) VALUES (
    NEW.id,
    NEW.manager_id,
    NEW.first_name,
    NEW.last_name,
    NEW.email,
    NEW.password,
    NEW.phone_number,
    NEW.street_address,
    NEW.city,
    NEW.created_at,
    NEW.updated_at
);

-- UPDATE TRIGGER
CREATE TRIGGER trg_agents_after_update
AFTER UPDATE ON agents FOR EACH ROW
INSERT INTO agents_history (
    id,
    manager_id,
    first_name,
    last_name,
    email,
    password,
    phone_number,
    street_address,
    city,
    created_at,
    updated_at
) VALUES (
    NEW.id,
    NEW.manager_id,
    NEW.first_name,
    NEW.last_name,
    NEW.email,
    NEW.password,
    NEW.phone_number,
    NEW.street_address,
    NEW.city,
    NEW.created_at,
    NEW.updated_at
);
