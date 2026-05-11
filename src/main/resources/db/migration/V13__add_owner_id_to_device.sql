-- ================================================
-- V13__add_owner_id_to_device.sql
-- Add owner_id to device table
-- ================================================

ALTER TABLE device
ADD COLUMN owner_id VARCHAR(36);

ALTER TABLE device
ADD CONSTRAINT fk_device_owner
FOREIGN KEY (owner_id) REFERENCES user(id) ON DELETE SET NULL;
