package com.autoflow.workorder.dto;

import com.autoflow.workorder.WorkOrderPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request body for creating a work order on a vehicle. The status always starts
 * as CREATED and is not client-controlled here.
 */
public record CreateWorkOrderRequest(

        @NotBlank(message = "Title must not be blank")
        @Size(max = 150, message = "Title must be at most 150 characters")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        // Optional; defaults to NORMAL when omitted.
        WorkOrderPriority priority,

        @PositiveOrZero(message = "Mileage at arrival cannot be negative")
        Integer mileageAtArrival,

        @Size(max = 2000, message = "Customer complaint must be at most 2000 characters")
        String customerComplaint,

        @Size(max = 2000, message = "Technician notes must be at most 2000 characters")
        String technicianNotes,

        LocalDate estimatedCompletionDate
) {
}
