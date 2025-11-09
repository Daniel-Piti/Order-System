-- MANAGERS TABLE
CREATE TABLE managers (
    id VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    business_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- MANAGERS HISTORY TABLE
CREATE TABLE managers_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    business_name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TRIGGER trg_managers_after_insert
AFTER INSERT ON managers FOR EACH ROW
INSERT INTO managers_history
    (id, first_name, last_name, email, business_name, password, phone_number, date_of_birth, street_address, city, created_at, updated_at)
VALUES (NEW.id, NEW.first_name, NEW.last_name, NEW.email, NEW.business_name, NEW.password, NEW.phone_number,
        NEW.date_of_birth, NEW.street_address, NEW.city, NEW.created_at, NEW.updated_at);

CREATE TRIGGER trg_managers_after_update
AFTER UPDATE ON managers FOR EACH ROW
INSERT INTO managers_history
    (id, first_name, last_name, email, business_name, password, phone_number, date_of_birth, street_address, city, created_at, updated_at)
VALUES (NEW.id, NEW.first_name, NEW.last_name, NEW.email, NEW.business_name, NEW.password, NEW.phone_number,
        NEW.date_of_birth, NEW.street_address, NEW.city, NEW.created_at, NEW.updated_at);
