-- PRODUCT OVERRIDES TABLE
CREATE TABLE product_overrides (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL,
    manager_id VARCHAR(255) NOT NULL,
    agent_id VARCHAR(36) NULL,
    customer_id VARCHAR(255) NOT NULL,
    override_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) AUTO_INCREMENT = 1000;

CREATE INDEX idx_product_overrides_product_id ON product_overrides (product_id);
CREATE INDEX idx_product_overrides_manager_id ON product_overrides (manager_id);
CREATE INDEX idx_product_overrides_agent_id ON product_overrides (agent_id);
CREATE INDEX idx_product_overrides_customer_id ON product_overrides (customer_id);
CREATE INDEX idx_product_overrides_manager_customer ON product_overrides (manager_id, customer_id);

-- PRODUCT OVERRIDES HISTORY TABLE
CREATE TABLE product_overrides_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id BIGINT NOT NULL,
    product_id VARCHAR(255) NOT NULL,
    manager_id VARCHAR(255) NOT NULL,
    agent_id VARCHAR(36) NULL,
    customer_id VARCHAR(255) NOT NULL,
    override_price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Trigger for INSERT
CREATE TRIGGER trg_product_overrides_after_insert
AFTER INSERT ON product_overrides FOR EACH ROW
INSERT INTO product_overrides_history (id, product_id, manager_id, agent_id, customer_id, override_price, created_at, updated_at)
VALUES (NEW.id, NEW.product_id, NEW.manager_id, NEW.agent_id, NEW.customer_id, NEW.override_price, NEW.created_at, NEW.updated_at);

-- Trigger for UPDATE
CREATE TRIGGER trg_product_overrides_after_update
AFTER UPDATE ON product_overrides FOR EACH ROW
INSERT INTO product_overrides_history (id, product_id, manager_id, agent_id, customer_id, override_price, created_at, updated_at)
VALUES (NEW.id, NEW.product_id, NEW.manager_id, NEW.agent_id, NEW.customer_id, NEW.override_price, NEW.created_at, NEW.updated_at);

