-- ================================================
-- V12: Add status_delay column to device table
-- ================================================

ALTER TABLE device
    ADD COLUMN status_relay BOOLEAN NOT NULL DEFAULT FALSE;
