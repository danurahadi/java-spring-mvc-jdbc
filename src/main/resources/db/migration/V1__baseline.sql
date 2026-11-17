CREATE TABLE users (
    id           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    status       VARCHAR(20)     NOT NULL,
    email        VARCHAR(320)        NULL,
    display_name VARCHAR(200)        NULL,
    picture_url  VARCHAR(1024)       NULL,
    created_at   DATETIME(6)     NOT NULL,
    updated_at   DATETIME(6)     NOT NULL,
    PRIMARY KEY (id),
    KEY idx_users_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_identities (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    user_id       BIGINT UNSIGNED NOT NULL,
    issuer        VARCHAR(255) COLLATE utf8mb4_0900_as_cs NOT NULL,
    subject       VARCHAR(255) COLLATE utf8mb4_0900_as_cs NOT NULL,
    provider      VARCHAR(50)     NOT NULL,
    created_at    DATETIME(6)     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_identities_issuer_subject (issuer, subject),
    KEY idx_user_identities_user_id (user_id),
    CONSTRAINT fk_user_identities_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE roles (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code        VARCHAR(64)     NOT NULL,
    name        VARCHAR(128)    NOT NULL,
    description VARCHAR(255)        NULL,
    created_at  DATETIME(6)     NOT NULL,
    updated_at  DATETIME(6)     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE permissions (
    id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code        VARCHAR(64)     NOT NULL,
    description VARCHAR(255)        NULL,
    created_at  DATETIME(6)     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_permissions_code (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_roles (
    user_id     BIGINT UNSIGNED NOT NULL,
    role_id     BIGINT UNSIGNED NOT NULL,
    assigned_at DATETIME(6)     NOT NULL,
    assigned_by BIGINT UNSIGNED     NULL,
    PRIMARY KEY (user_id, role_id),
    KEY idx_user_roles_role_id (role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_user_roles_assigned_by FOREIGN KEY (assigned_by) REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE role_permissions (
    role_id       BIGINT UNSIGNED NOT NULL,
    permission_id BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    KEY idx_role_permissions_permission_id (permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE audit_log (
    id            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    occurred_at   DATETIME(6)     NOT NULL,
    actor_user_id BIGINT UNSIGNED     NULL,
    actor_subject VARCHAR(255)        NULL,
    action        VARCHAR(64)     NOT NULL,
    target_type   VARCHAR(32)     NOT NULL,
    target_id     VARCHAR(64)         NULL,
    details       JSON                NULL,
    PRIMARY KEY (id),
    KEY idx_audit_log_actor (actor_user_id),
    KEY idx_audit_log_occurred_at (occurred_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
