-- V15__add_device_ownership_and_mqtt.sql

-- Cho phép thiết bị không gắn với user nào (UNCLAIMED)
ALTER TABLE device MODIFY user_id VARCHAR(36) NULL;

-- Thêm định danh phần cứng (MAC/Serial) để ESP đăng ký tự động
ALTER TABLE device ADD COLUMN chip_id VARCHAR(50) UNIQUE;

-- Ghi nhận thời điểm chủ sở hữu claim thiết bị
ALTER TABLE device ADD COLUMN claimed_at TIMESTAMP NULL;
