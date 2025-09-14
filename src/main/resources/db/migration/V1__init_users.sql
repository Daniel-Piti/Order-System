-- USERS TABLE
CREATE TABLE users (
    id VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    main_address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- USERS HISTORY TABLE
CREATE TABLE users_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    main_address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TRIGGER trg_users_after_insert
AFTER INSERT ON users FOR EACH ROW
INSERT INTO users_history
    (id, first_name, last_name, email, password, phone_number, date_of_birth, main_address, created_at, updated_at)
VALUES (NEW.id, NEW.first_name, NEW.last_name, NEW.email, NEW.password, NEW.phone_number,
        NEW.date_of_birth, NEW.main_address, NEW.created_at, NEW.updated_at);

CREATE TRIGGER trg_users_after_update
AFTER UPDATE ON users FOR EACH ROW
INSERT INTO users_history
    (id, first_name, last_name, email, password, phone_number, date_of_birth, main_address, created_at, updated_at)
VALUES (NEW.id, NEW.first_name, NEW.last_name, NEW.email, NEW.password, NEW.phone_number,
        NEW.date_of_birth, NEW.main_address, NEW.created_at, NEW.updated_at);
