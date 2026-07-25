package com.autoflow.workorder.dto;

import java.math.BigDecimal;

/**
 * Response body for a work order line, including the computed line total.
 */
public record WorkOrderLineResponse(
        Long id,
        Long workOrderId,
        String description,
        String partNumber,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
