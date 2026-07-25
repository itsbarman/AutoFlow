package com.autoflow.workorder;

import com.autoflow.customer.Customer;
import com.autoflow.vehicle.Vehicle;
import com.autoflow.workorder.dto.CreateWorkOrderRequest;
import com.autoflow.workorder.dto.UpdateWorkOrderRequest;
import com.autoflow.workorder.dto.WorkOrderResponse;
import org.springframework.stereotype.Component;

/**
 * Converts between {@link WorkOrder} entities and the work order DTOs.
 */
@Component
public class WorkOrderMapper {

    /**
     * Builds a new work order from a create request. The caller supplies the
     * generated number, the owning vehicle and its customer.
     */
    public WorkOrder toEntity(CreateWorkOrderRequest request, String workOrderNumber,
                              Vehicle vehicle, Customer customer) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderNumber(workOrderNumber);
        workOrder.setVehicle(vehicle);
        workOrder.setCustomer(customer);
        workOrder.setTitle(request.title());
        workOrder.setDescription(request.description());
        workOrder.setPriority(request.priority() != null ? request.priority() : WorkOrderPriority.NORMAL);
        workOrder.setStatus(WorkOrderStatus.CREATED);
        workOrder.setMileageAtArrival(request.mileageAtArrival());
        workOrder.setCustomerComplaint(request.customerComplaint());
        workOrder.setTechnicianNotes(request.technicianNotes());
        workOrder.setEstimatedCompletionDate(request.estimatedCompletionDate());
        return workOrder;
    }

    /**
     * Copies editable fields from an update request onto an existing entity.
     * Status is handled separately.
     */
    public void updateEntity(WorkOrder workOrder, UpdateWorkOrderRequest request) {
        workOrder.setTitle(request.title());
        workOrder.setDescription(request.description());
        workOrder.setPriority(request.priority());
        workOrder.setMileageAtArrival(request.mileageAtArrival());
        workOrder.setCustomerComplaint(request.customerComplaint());
        workOrder.setTechnicianNotes(request.technicianNotes());
        workOrder.setEstimatedCompletionDate(request.estimatedCompletionDate());
    }

    public WorkOrderResponse toResponse(WorkOrder workOrder) {
        Customer customer = workOrder.getCustomer();
        Vehicle vehicle = workOrder.getVehicle();
        return new WorkOrderResponse(
                workOrder.getId(),
                workOrder.getWorkOrderNumber(),
                workOrder.getTitle(),
                workOrder.getDescription(),
                workOrder.getStatus(),
                workOrder.getPriority(),
                workOrder.getMileageAtArrival(),
                workOrder.getCustomerComplaint(),
                workOrder.getTechnicianNotes(),
                workOrder.getEstimatedCompletionDate(),
                customer.getId(),
                customer.getFirstName() + " " + customer.getLastName(),
                vehicle.getId(),
                vehicle.getRegistrationNumber(),
                vehicle.getMake() + " " + vehicle.getModel(),
                workOrder.getCreatedAt(),
                workOrder.getUpdatedAt(),
                workOrder.getCompletedAt()
        );
    }
}
