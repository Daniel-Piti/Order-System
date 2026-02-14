-- BUSINESS DETAILS TABLE
CREATE TABLE business_details (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    manager_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    state_id_number VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    s3_key VARCHAR(512) NULL,
    file_name VARCHAR(255) NULL,
    file_size_bytes BIGINT NULL,
    mime_type VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_business_details_manager_id (manager_id)
);

-- BUSINESS DETAILS HISTORY TABLE
CREATE TABLE business_details_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    manager_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    state_id_number VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    street_address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    s3_key VARCHAR(512) NULL,
    file_name VARCHAR(255) NULL,
    file_size_bytes BIGINT NULL,
    mime_type VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Trigger for INSERT
CREATE TRIGGER trg_business_details_after_insert
AFTER INSERT ON business_details FOR EACH ROW
INSERT INTO business_details_history
    (id, manager_id, name, state_id_number, email, phone_number, street_address, city, s3_key, file_name, file_size_bytes, mime_type, created_at, updated_at)
VALUES (NEW.id, NEW.manager_id, NEW.name, NEW.state_id_number, NEW.email, NEW.phone_number,
        NEW.street_address, NEW.city, NEW.s3_key, NEW.file_name, NEW.file_size_bytes, NEW.mime_type, NEW.created_at, NEW.updated_at);

-- Trigger for UPDATE
CREATE TRIGGER trg_business_details_after_update
AFTER UPDATE ON business_details FOR EACH ROW
INSERT INTO business_details_history
    (id, manager_id, name, state_id_number, email, phone_number, street_address, city, s3_key, file_name, file_size_bytes, mime_type, created_at, updated_at)
VALUES (NEW.id, NEW.manager_id, NEW.name, NEW.state_id_number, NEW.email, NEW.phone_number,
        NEW.street_address, NEW.city, NEW.s3_key, NEW.file_name, NEW.file_size_bytes, NEW.mime_type, NEW.created_at, NEW.updated_at);

