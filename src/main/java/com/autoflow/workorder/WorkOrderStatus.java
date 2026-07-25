package com.autoflow.workorder;

/**
 * The lifecycle state of a work order.
 */
public enum WorkOrderStatus {
    CREATED,
    BOOKED,
    IN_PROGRESS,
    WAITING_FOR_PARTS,
    COMPLETED,
    CANCELLED
}
