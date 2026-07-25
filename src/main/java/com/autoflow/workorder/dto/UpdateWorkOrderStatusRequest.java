package com.autoflow.workorder.dto;

import com.autoflow.workorder.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for the PATCH status endpoint.
 */
public record UpdateWorkOrderStatusRequest(

        @NotNull(message = "Status is required")
        WorkOrderStatus status
) {
}
