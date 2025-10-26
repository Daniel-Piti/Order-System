-- LOCATIONS TABLE
CREATE TABLE locations (
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE INDEX idx_locations_user_id ON locations (user_id);

-- LOCATIONS HISTORY TABLE
CREATE TABLE locations_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Trigger for INSERT
CREATE TRIGGER trg_locations_after_insert
AFTER INSERT ON locations
FOR EACH ROW
INSERT INTO locations_history (id, user_id, name, street_address, city, phone_number, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.street_address, NEW.city, NEW.phone_number, NEW.created_at, NEW.updated_at);

-- Trigger for UPDATE
CREATE TRIGGER trg_locations_after_update
AFTER UPDATE ON locations
FOR EACH ROW
INSERT INTO locations_history (id, user_id, name, street_address, city, phone_number, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.street_address, NEW.city, NEW.phone_number, NEW.created_at, NEW.updated_at);
