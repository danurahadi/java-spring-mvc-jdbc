-- Dummy data for local/dev only. The issuer is a non-resolvable placeholder, so no real Auth0 token can map to these identities.
CREATE TEMPORARY TABLE seed_users
(
    email          VARCHAR(50) NOT NULL,
    display_name   VARCHAR(50) NOT NULL,
    contact_number VARCHAR(10) NOT NULL,
    role_code      VARCHAR(64) NOT NULL,
    subject        VARCHAR(255) NOT NULL
) ENGINE = InnoDB;

INSERT INTO seed_users (email, display_name, contact_number, role_code, subject)
VALUES ('seed01@example.test', 'Seed Admin One', '0810000001', 'admin', 'auth0|seed-user-01'),
       ('seed02@example.test', 'Seed Admin Two', '0810000002', 'agent', 'auth0|seed-user-02'),
       ('seed03@example.test', 'Seed Admin Three', '0810000003', 'agent', 'auth0|seed-user-03'),
       ('seed04@example.test', 'Seed Admin Four', '0810000004', 'agent', 'auth0|seed-user-04'),
       ('seed05@example.test', 'Seed Admin Five', '0810000005', 'agent', 'auth0|seed-user-05'),
       ('seed06@example.test', 'Seed Member One', '0810000006', 'agent', 'auth0|seed-user-06'),
       ('seed07@example.test', 'Seed Member Two', '0810000007', 'agent', 'auth0|seed-user-07'),
       ('seed08@example.test', 'Seed Member Three', '0810000008', 'agent', 'auth0|seed-user-08'),
       ('seed09@example.test', 'Seed Member Four', '0810000009', 'agent', 'auth0|seed-user-09'),
       ('seed10@example.test', 'Seed Member Five', '0810000010', 'agent', 'auth0|seed-user-10');

INSERT INTO users (status, email, display_name, contact_number, created_at, updated_at)
SELECT 'ACTIVE', email, display_name, contact_number, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM seed_users;

INSERT INTO user_identities (user_id, issuer, subject, provider, created_at)
SELECT u.id, 'https://seed.invalid/', s.subject, 'auth0', UTC_TIMESTAMP(6)
FROM seed_users s
         JOIN users u ON u.email = s.email;

INSERT INTO user_roles (user_id, role_id, assigned_at, assigned_by)
SELECT u.id, r.id, UTC_TIMESTAMP(6), NULL
FROM seed_users s
         JOIN users u ON u.email = s.email
         JOIN roles r ON r.code = s.role_code;

DROP TEMPORARY TABLE seed_users;
