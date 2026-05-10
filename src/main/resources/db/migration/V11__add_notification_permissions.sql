INSERT INTO permission (id, name, description) VALUES
                                                   ('perm-notif-read',   'NOTIFICATION_READ',   'Read notifications'),
                                                   ('perm-notif-update', 'NOTIFICATION_UPDATE', 'Mark notifications as read'),
                                                   ('perm-notif-delete', 'NOTIFICATION_DELETE', 'Delete notifications');

INSERT INTO role_permissions (role_id, permission_id) VALUES
                                                          ('role-1', 'perm-notif-read'),
                                                          ('role-1', 'perm-notif-update'),
                                                          ('role-1', 'perm-notif-delete');