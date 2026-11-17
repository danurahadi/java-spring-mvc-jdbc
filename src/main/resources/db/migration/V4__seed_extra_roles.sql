INSERT INTO roles (code, name, description, created_at, updated_at)
VALUES ('agent', 'Agent', 'Read access to users, roles and permissions', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         JOIN permissions p ON p.code IN ('users:read', 'roles:read')
WHERE r.code = 'agent';
