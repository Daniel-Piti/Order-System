-- BRANDS TABLE
CREATE TABLE brands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    s3_key VARCHAR(512),
    file_name VARCHAR(255),
    file_size_bytes BIGINT,
    mime_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_brands_user_name UNIQUE (user_id, name)
);

CREATE INDEX idx_brands_user_id ON brands(user_id);

-- BRANDS HISTORY TABLE
CREATE TABLE brands_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    s3_key VARCHAR(512),
    file_name VARCHAR(255),
    file_size_bytes BIGINT,
    mime_type VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- INSERT TRIGGER
CREATE TRIGGER trg_brands_after_insert
AFTER INSERT ON brands FOR EACH ROW
INSERT INTO brands_history (id, user_id, name, s3_key, file_name, file_size_bytes, mime_type, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.s3_key, NEW.file_name, NEW.file_size_bytes, NEW.mime_type, NEW.created_at, NEW.updated_at);

-- UPDATE TRIGGER
CREATE TRIGGER trg_brands_after_update
AFTER UPDATE ON brands FOR EACH ROW
INSERT INTO brands_history (id, user_id, name, s3_key, file_name, file_size_bytes, mime_type, created_at, updated_at)
VALUES (NEW.id, NEW.user_id, NEW.name, NEW.s3_key, NEW.file_name, NEW.file_size_bytes, NEW.mime_type, NEW.created_at, NEW.updated_at);

