-- V16__add_esp_fields_to_device.sql

ALTER TABLE device CHANGE status_delay status_relay BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE device ADD COLUMN wifi_rssi INT NULL;
ALTER TABLE device ADD COLUMN ip VARCHAR(255) NULL;
ALTER TABLE device ADD COLUMN free_heap BIGINT NULL;
ALTER TABLE device ADD COLUMN uptime BIGINT NULL;
