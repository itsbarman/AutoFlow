-- V3: work order schema. A work order belongs to one vehicle and its owning customer.
-- A customer/vehicle can have many work orders.

-- Sequence used to generate human-readable, unique work order numbers (WO-000001, ...).
CREATE SEQUENCE work_order_number_seq START 1;

CREATE TABLE work_orders (
    id                        BIGSERIAL PRIMARY KEY,
    work_order_number         VARCHAR(20)  NOT NULL,
    customer_id               BIGINT       NOT NULL REFERENCES customers (id),
    vehicle_id                BIGINT       NOT NULL REFERENCES vehicles (id),
    title                     VARCHAR(150) NOT NULL,
    description               VARCHAR(2000),
    status                    VARCHAR(30)  NOT NULL,
    priority                  VARCHAR(20)  NOT NULL,
    mileage_at_arrival        INTEGER,
    customer_complaint        VARCHAR(2000),
    technician_notes          VARCHAR(2000),
    estimated_completion_date DATE,
    created_at                TIMESTAMPTZ  NOT NULL,
    updated_at                TIMESTAMPTZ  NOT NULL,
    completed_at              TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_work_orders_number      ON work_orders (work_order_number);
CREATE INDEX        idx_work_orders_customer_id ON work_orders (customer_id);
CREATE INDEX        idx_work_orders_vehicle_id  ON work_orders (vehicle_id);
CREATE INDEX        idx_work_orders_status      ON work_orders (status);

-- No ON DELETE CASCADE: the database and the service both refuse to delete a
-- vehicle (or customer) that still has work orders.
