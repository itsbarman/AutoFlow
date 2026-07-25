-- V5: line items (parts/labor) that make up a work order.

CREATE TABLE work_order_lines (
    id            BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT        NOT NULL REFERENCES work_orders (id) ON DELETE CASCADE,
    description   VARCHAR(200)  NOT NULL,
    part_number   VARCHAR(60),
    quantity      NUMERIC(10, 2) NOT NULL,
    unit_price    NUMERIC(10, 2) NOT NULL,
    created_at    TIMESTAMPTZ   NOT NULL,
    updated_at    TIMESTAMPTZ   NOT NULL
);

CREATE INDEX idx_work_order_lines_work_order_id ON work_order_lines (work_order_id);

-- Lines are part of the work order aggregate: ON DELETE CASCADE removes them when
-- the work order itself is deleted. (Customers/vehicles/work orders remain protected.)
