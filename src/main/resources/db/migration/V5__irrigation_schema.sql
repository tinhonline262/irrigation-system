-- ================================================
-- V5__irrigation_schema.sql
-- IoT device, watering log & schedule
-- ================================================

CREATE TABLE device
(
    id                      VARCHAR(36)  PRIMARY KEY,
    ol                      BIGINT       NOT NULL DEFAULT 0,
    created_at              TIMESTAMP,
    created_by              VARCHAR(255),
    last_modified_at        TIMESTAMP,
    last_modified_by        VARCHAR(255),
    user_id                 VARCHAR(36)  NOT NULL,
    name                    VARCHAR(255) NOT NULL,
    status                  VARCHAR(50)  NOT NULL DEFAULT 'offline', -- 'online' | 'offline' | 'error'
    moisture_threshold_low  FLOAT,
    moisture_threshold_high FLOAT,
    auto_water_enabled      BOOLEAN      NOT NULL DEFAULT FALSE,
    last_seen_at            TIMESTAMP,
    CONSTRAINT fk_device_user
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);

CREATE TABLE watering_log
(
    id              VARCHAR(36) PRIMARY KEY,
    device_id       VARCHAR(36) NOT NULL,
    triggered_by    VARCHAR(36),                -- NULL nếu auto hoặc schedule
    trigger_type    VARCHAR(50) NOT NULL,        -- 'manual' | 'auto' | 'schedule'
    started_at      TIMESTAMP   NOT NULL,
    ended_at        TIMESTAMP,
    water_amount_ml FLOAT,
    CONSTRAINT fk_wlog_device
        FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE CASCADE,
    CONSTRAINT fk_wlog_user
        FOREIGN KEY (triggered_by) REFERENCES user(id) ON DELETE SET NULL
);

CREATE INDEX idx_wlog_device_time ON watering_log (device_id, started_at DESC);

CREATE TABLE watering_schedule
(
    id               VARCHAR(36)  PRIMARY KEY,
    ol               BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMP,
    created_by       VARCHAR(255),
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),
    device_id        VARCHAR(36)  NOT NULL,
    cron_expression  VARCHAR(100) NOT NULL,      -- vd: '0 6 * * *' = 6h sáng mỗi ngày
    water_amount_ml  FLOAT        NOT NULL,
    enabled          BOOLEAN      NOT NULL DEFAULT TRUE,
    next_run_at      TIMESTAMP,
    CONSTRAINT fk_schedule_device
        FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE CASCADE
);