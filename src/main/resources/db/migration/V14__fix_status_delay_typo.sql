-- ================================================
-- V14__fix_status_delay_typo.sql
-- Fix typo in V12: rename status_relay to status_delay
-- ================================================

ALTER TABLE device
CHANGE COLUMN status_relay status_delay BOOLEAN NOT NULL DEFAULT FALSE;
