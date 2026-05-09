-- V8__add_admin_permissions_and_calibration.sql

-- Add calibration fields to device
ALTER TABLE device ADD COLUMN soil_moisture_offset FLOAT DEFAULT 0.0;
ALTER TABLE device ADD COLUMN air_temperature_offset FLOAT DEFAULT 0.0;
ALTER TABLE device ADD COLUMN air_humidity_offset FLOAT DEFAULT 0.0;

-- Additional Permissions for Admin APIs
INSERT INTO permission (id, name, description) VALUES
    ('perm-5', 'DEVICE_READ_ALL', 'Read all devices in the system'),
    ('perm-6', 'DEVICE_DELETE', 'Delete any device'),
    ('perm-7', 'DEVICE_CALIBRATE', 'Calibrate device sensors'),
    ('perm-8', 'USER_READ_ALL', 'Read all users in the system'),
    ('perm-9', 'USER_UPDATE_ROLE', 'Update user roles'),
    ('perm-10', 'ROLE_CREATE', 'Create new roles'),
    ('perm-11', 'ROLE_UPDATE', 'Update role permissions'),
    ('perm-12', 'ROLE_READ_ALL', 'Read all roles in the system');

INSERT INTO permission (
    id,
    name,
    description
) VALUES (
             'perm-13',
             'DEVICE_CREATE',
             'Create new devices'
         );

-- Assign new permissions to ADMIN role (role-1)
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('role-1', 'perm-5'),
    ('role-1', 'perm-6'),
    ('role-1', 'perm-7'),
    ('role-1', 'perm-8'),
    ('role-1', 'perm-9'),
    ('role-1', 'perm-10'),
    ('role-1', 'perm-11'),
    ('role-1', 'perm-12'),
    ('role-1', 'perm-13');
