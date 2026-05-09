-- Add Device Config Permissions
INSERT INTO permission (id, name, description) VALUES
                                                   ('perm-device-config-read',   'DEVICE_CONFIG_READ',   'Read device config'),
                                                   ('perm-device-config-update', 'DEVICE_CONFIG_UPDATE', 'Update device config');

-- Grant to ADMIN role
INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          ('role-1', 'perm-device-config-read'),
                                                          ('role-1', 'perm-device-config-update');