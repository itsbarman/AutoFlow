-- V2: vehicle schema. A customer can own many vehicles; a vehicle belongs to one customer.

CREATE TABLE vehicles (
    id                  BIGSERIAL PRIMARY KEY,
    customer_id         BIGINT       NOT NULL REFERENCES customers (id),
    registration_number VARCHAR(15)  NOT NULL,
    vin                 VARCHAR(17),
    make                VARCHAR(60)  NOT NULL,
    model               VARCHAR(60)  NOT NULL,
    model_year          INTEGER,
    mileage             INTEGER,
    fuel_type           VARCHAR(20)  NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);

-- Registration number is always unique. VIN is unique only when provided
-- (a partial index lets several vehicles have a NULL vin).
CREATE UNIQUE INDEX ux_vehicles_registration_number ON vehicles (registration_number);
CREATE UNIQUE INDEX ux_vehicles_vin ON vehicles (vin) WHERE vin IS NOT NULL;
CREATE INDEX idx_vehicles_customer_id ON vehicles (customer_id);

-- Note: the foreign key has no ON DELETE CASCADE on purpose. The database will
-- refuse to delete a customer that still owns vehicles; the service also checks
-- this first and returns a friendly error.
