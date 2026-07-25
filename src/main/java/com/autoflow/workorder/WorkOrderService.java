package com.autoflow.workorder;

import com.autoflow.common.exception.ResourceNotFoundException;
import com.autoflow.vehicle.Vehicle;
import com.autoflow.vehicle.VehicleRepository;
import com.autoflow.workorder.dto.CreateWorkOrderRequest;
import com.autoflow.workorder.dto.UpdateWorkOrderRequest;
import com.autoflow.workorder.dto.WorkOrderResponse;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for work orders: number generation, status transitions and the
 * links to a vehicle and its owning customer.
 */
@Service
@Transactional
public class WorkOrderService {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderService.class);

    private final WorkOrderRepository workOrderRepository;
    private final VehicleRepository vehicleRepository;
    private final WorkOrderMapper workOrderMapper;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                            VehicleRepository vehicleRepository,
                            WorkOrderMapper workOrderMapper) {
        this.workOrderRepository = workOrderRepository;
        this.vehicleRepository = vehicleRepository;
        this.workOrderMapper = workOrderMapper;
    }

    public WorkOrderResponse createWorkOrder(Long vehicleId, CreateWorkOrderRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> ResourceNotFoundException.of("Kjøretøy", vehicleId));

        // The customer is always derived from the vehicle's owner.
        String number = generateWorkOrderNumber();
        WorkOrder workOrder = workOrderMapper.toEntity(request, number, vehicle, vehicle.getCustomer());
        WorkOrder saved = workOrderRepository.save(workOrder);
        log.info("Work order {} created for vehicle {}", saved.getWorkOrderNumber(), vehicleId);
        return workOrderMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> getAllWorkOrders() {
        return workOrderRepository.findAll().stream().map(workOrderMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> getWorkOrdersByVehicle(Long vehicleId) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw ResourceNotFoundException.of("Kjøretøy", vehicleId);
        }
        return workOrderRepository.findByVehicleIdOrderByIdAsc(vehicleId).stream()
                .map(workOrderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse getWorkOrderById(Long id) {
        return workOrderMapper.toResponse(findWorkOrderOrThrow(id));
    }

    public WorkOrderResponse updateWorkOrder(Long id, UpdateWorkOrderRequest request) {
        WorkOrder workOrder = findWorkOrderOrThrow(id);
        workOrderMapper.updateEntity(workOrder, request);
        WorkOrder saved = workOrderRepository.save(workOrder);
        log.info("Work order {} updated", saved.getWorkOrderNumber());
        return workOrderMapper.toResponse(saved);
    }

    /**
     * Changes only the status. Sets {@code completedAt} when moving to COMPLETED
     * and clears it otherwise.
     */
    public WorkOrderResponse updateStatus(Long id, WorkOrderStatus newStatus) {
        WorkOrder workOrder = findWorkOrderOrThrow(id);
        WorkOrderStatus previous = workOrder.getStatus();
        workOrder.setStatus(newStatus);
        workOrder.setCompletedAt(newStatus == WorkOrderStatus.COMPLETED ? Instant.now() : null);
        WorkOrder saved = workOrderRepository.save(workOrder);
        log.info("Work order {} status changed from {} to {}",
                saved.getWorkOrderNumber(), previous, newStatus);
        return workOrderMapper.toResponse(saved);
    }

    public void deleteWorkOrder(Long id) {
        WorkOrder workOrder = findWorkOrderOrThrow(id);
        workOrderRepository.delete(workOrder);
        log.info("Work order {} deleted", workOrder.getWorkOrderNumber());
    }

    private WorkOrder findWorkOrderOrThrow(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Arbeidsordre", id));
    }

    /** Produces a readable, unique number such as "WO-000042". */
    private String generateWorkOrderNumber() {
        long sequence = workOrderRepository.nextWorkOrderNumberSequence();
        return String.format("WO-%06d", sequence);
    }
}
