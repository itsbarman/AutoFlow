-- V4: application users and their roles for authentication/authorization.

CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(100) NOT NULL,
    password   VARCHAR(100) NOT NULL,
    full_name  VARCHAR(150) NOT NULL,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL
);

CREATE UNIQUE INDEX ux_users_username ON users (LOWER(username));

-- A user can have several roles. Role names are stored as text (ADMIN, MECHANIC, RECEPTIONIST).
CREATE TABLE user_roles (
    user_id BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role    VARCHAR(30) NOT NULL,
    PRIMARY KEY (user_id, role)
);
