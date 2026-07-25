package com.autoflow.workorder.dto;

import com.autoflow.workorder.WorkOrderPriority;
import com.autoflow.workorder.WorkOrderStatus;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Response body for a work order, including small summaries of the linked
 * customer and vehicle so the UI can render them without extra requests.
 */
public record WorkOrderResponse(
        Long id,
        String workOrderNumber,
        String title,
        String description,
        WorkOrderStatus status,
        WorkOrderPriority priority,
        Integer mileageAtArrival,
        String customerComplaint,
        String technicianNotes,
        LocalDate estimatedCompletionDate,
        Long customerId,
        String customerName,
        Long vehicleId,
        String vehicleRegistrationNumber,
        String vehicleDescription,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt
) {
}
