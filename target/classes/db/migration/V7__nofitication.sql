-- ================================================
-- V7__notification.sql
-- Thông báo gửi đến người dùng
-- ================================================

CREATE TABLE notification
(
    id               VARCHAR(36)  PRIMARY KEY,
    ol               BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMP,
    created_by       VARCHAR(255),
    last_modified_at TIMESTAMP,
    last_modified_by VARCHAR(255),
    user_id          VARCHAR(36)  NOT NULL,
    device_id        VARCHAR(36),                -- NULL nếu thông báo hệ thống
    title            VARCHAR(255) NOT NULL,
    message          TEXT         NOT NULL,
    type             VARCHAR(50)  NOT NULL,       -- 'watering_done' | 'low_moisture' | 'device_offline' | 'schedule_triggered'
    is_read          BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at          TIMESTAMP,
    CONSTRAINT fk_notif_user
        FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_device
        FOREIGN KEY (device_id) REFERENCES device(id) ON DELETE SET NULL
);

-- Tối ưu query lấy thông báo chưa đọc, sắp xếp mới nhất
CREATE INDEX idx_notif_user_read ON notification (user_id, is_read, created_at DESC);