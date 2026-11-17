INSERT INTO permissions (code, description, created_at)
VALUES ('users:read', 'Read application users', UTC_TIMESTAMP(6)),
       ('users:update', 'Update application user profile, email and account status', UTC_TIMESTAMP(6)),
       ('users:assign-roles', 'Assign and remove application roles for users', UTC_TIMESTAMP(6)),
       ('roles:read', 'Read roles and permissions', UTC_TIMESTAMP(6)),
       ('roles:write', 'Create roles and change role permission assignments', UTC_TIMESTAMP(6));

INSERT INTO roles (code, name, description, created_at, updated_at)
VALUES ('admin', 'Administrator', 'Full administrative access to users, roles and permissions',
        UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE r.code = 'admin';
