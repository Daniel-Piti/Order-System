-- PRODUCT IMAGES TABLE
CREATE TABLE product_images (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    s3_key VARCHAR(512) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_product_images_product_id (product_id),
    INDEX idx_product_images_user_id (user_id)
);

-- PRODUCT IMAGES HISTORY TABLE
CREATE TABLE product_images_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id VARCHAR(255) NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    s3_key VARCHAR(512) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- INSERT TRIGGER
CREATE TRIGGER trg_product_images_after_insert
AFTER INSERT ON product_images FOR EACH ROW
INSERT INTO product_images_history (id, product_id, user_id, s3_key, file_name, file_size_bytes, mime_type, created_at, updated_at)
VALUES (NEW.id, NEW.product_id, NEW.user_id, NEW.s3_key, NEW.file_name, NEW.file_size_bytes, NEW.mime_type, NEW.created_at, NEW.updated_at);

-- UPDATE TRIGGER
CREATE TRIGGER trg_product_images_after_update
AFTER UPDATE ON product_images FOR EACH ROW
INSERT INTO product_images_history (id, product_id, user_id, s3_key, file_name, file_size_bytes, mime_type, created_at, updated_at)
VALUES (NEW.id, NEW.product_id, NEW.user_id, NEW.s3_key, NEW.file_name, NEW.file_size_bytes, NEW.mime_type, NEW.created_at, NEW.updated_at);
