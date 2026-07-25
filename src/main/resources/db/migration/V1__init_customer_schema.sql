-- V1: initial schema for the AutoFlow customer domain.
-- Naming convention: snake_case for tables and columns.

CREATE TABLE customers (
    id            BIGSERIAL PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    phone_number  VARCHAR(30)  NOT NULL,
    email         VARCHAR(255),
    address       VARCHAR(255),
    postal_code   VARCHAR(20),
    city          VARCHAR(100),
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);

-- Indexes to speed up the most common lookups.
-- Email is intentionally NOT unique: several customers may share one address.
CREATE INDEX idx_customers_last_name   ON customers (last_name);
CREATE INDEX idx_customers_phone_number ON customers (phone_number);
