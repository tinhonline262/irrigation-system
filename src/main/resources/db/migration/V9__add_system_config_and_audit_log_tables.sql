CREATE TABLE system_config (
    `key` VARCHAR(100) PRIMARY KEY,
    ol BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),
    `value` TEXT NOT NULL
);

CREATE TABLE audit_log (
    id VARCHAR(36) PRIMARY KEY,
    ol BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP,
    created_by VARCHAR(255),
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),
    user_id VARCHAR(36) NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_id VARCHAR(36),
    payload JSON,
    CONSTRAINT fk_audit_log_user
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
