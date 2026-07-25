package com.autoflow.workorder.dto;

import com.autoflow.workorder.WorkOrderPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Request body for a full update (PUT) of a work order's editable fields.
 * Status is changed through its own PATCH endpoint, not here.
 */
public record UpdateWorkOrderRequest(

        @NotBlank(message = "Tittel kan ikke være tom")
        @Size(max = 150, message = "Tittel kan være maks 150 tegn")
        String title,

        @Size(max = 2000, message = "Beskrivelse kan være maks 2000 tegn")
        String description,

        @NotNull(message = "Prioritet er påkrevd")
        WorkOrderPriority priority,

        @PositiveOrZero(message = "Kilometerstand ved ankomst kan ikke være negativ")
        Integer mileageAtArrival,

        @Size(max = 2000, message = "Kundens beskrivelse kan være maks 2000 tegn")
        String customerComplaint,

        @Size(max = 2000, message = "Tekniker-notater kan være maks 2000 tegn")
        String technicianNotes,

        LocalDate estimatedCompletionDate
) {
}
