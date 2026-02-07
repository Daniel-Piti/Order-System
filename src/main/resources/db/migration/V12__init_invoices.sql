-- INVOICES TABLE
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    manager_id VARCHAR(255) NOT NULL,
    order_id BIGINT NOT NULL UNIQUE,
    invoice_sequence_number INT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_proof VARCHAR(512) NOT NULL,
    allocation_number VARCHAR(9) NULL,
    s3_key VARCHAR(512) NULL,
    file_name VARCHAR(255) NULL,
    file_size_bytes BIGINT NULL,
    mime_type VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_invoices_manager_id (manager_id),
    INDEX idx_invoices_order_id (order_id),
    INDEX idx_invoices_manager_sequence (manager_id, invoice_sequence_number),
    UNIQUE KEY uk_invoices_order_id (order_id)
) AUTO_INCREMENT = 1000;

-- INVOICES HISTORY TABLE
CREATE TABLE invoices_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id BIGINT NOT NULL,
    manager_id VARCHAR(255) NOT NULL,
    order_id BIGINT NOT NULL,
    invoice_sequence_number INT NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_proof VARCHAR(512) NOT NULL,
    allocation_number VARCHAR(9) NULL,
    s3_key VARCHAR(512) NULL,
    file_name VARCHAR(255) NULL,
    file_size_bytes BIGINT NULL,
    mime_type VARCHAR(100) NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- INSERT TRIGGER
CREATE TRIGGER trg_invoices_after_insert
AFTER INSERT ON invoices FOR EACH ROW
INSERT INTO invoices_history (
    id, manager_id, order_id, invoice_sequence_number,
    payment_method, payment_proof, allocation_number,
    s3_key, file_name, file_size_bytes, mime_type,
    created_at, updated_at
)
VALUES (
    NEW.id, NEW.manager_id, NEW.order_id, NEW.invoice_sequence_number,
    NEW.payment_method, NEW.payment_proof, NEW.allocation_number,
    NEW.s3_key, NEW.file_name, NEW.file_size_bytes, NEW.mime_type,
    NEW.created_at, NEW.updated_at
);

-- UPDATE TRIGGER
CREATE TRIGGER trg_invoices_after_update
AFTER UPDATE ON invoices FOR EACH ROW
INSERT INTO invoices_history (
    id, manager_id, order_id, invoice_sequence_number,
    payment_method, payment_proof, allocation_number,
    s3_key, file_name, file_size_bytes, mime_type,
    created_at, updated_at
)
VALUES (
    NEW.id, NEW.manager_id, NEW.order_id, NEW.invoice_sequence_number,
    NEW.payment_method, NEW.payment_proof, NEW.allocation_number,
    NEW.s3_key, NEW.file_name, NEW.file_size_bytes, NEW.mime_type,
    NEW.created_at, NEW.updated_at
);
