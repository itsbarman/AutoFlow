package com.autoflow.workorder;

import com.autoflow.workorder.dto.CreateWorkOrderLineRequest;
import com.autoflow.workorder.dto.UpdateWorkOrderLineRequest;
import com.autoflow.workorder.dto.WorkOrderLineResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderLineMapper {

    public WorkOrderLine toEntity(CreateWorkOrderLineRequest request, WorkOrder workOrder) {
        WorkOrderLine line = new WorkOrderLine();
        line.setWorkOrder(workOrder);
        line.setDescription(request.description());
        line.setPartNumber(request.partNumber());
        line.setQuantity(request.quantity());
        line.setUnitPrice(request.unitPrice());
        return line;
    }

    public void updateEntity(WorkOrderLine line, UpdateWorkOrderLineRequest request) {
        line.setDescription(request.description());
        line.setPartNumber(request.partNumber());
        line.setQuantity(request.quantity());
        line.setUnitPrice(request.unitPrice());
    }

    public WorkOrderLineResponse toResponse(WorkOrderLine line) {
        return new WorkOrderLineResponse(
                line.getId(),
                line.getWorkOrder().getId(),
                line.getDescription(),
                line.getPartNumber(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getLineTotal()
        );
    }
}
