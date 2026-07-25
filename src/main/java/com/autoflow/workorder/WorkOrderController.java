package com.autoflow.workorder;

import com.autoflow.workorder.dto.CreateWorkOrderRequest;
import com.autoflow.workorder.dto.UpdateWorkOrderRequest;
import com.autoflow.workorder.dto.UpdateWorkOrderStatusRequest;
import com.autoflow.workorder.dto.WorkOrderResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST endpoints for work orders. Creation is scoped under a vehicle; the other
 * operations act on a work order by its own id.
 */
@RestController
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping("/api/v1/vehicles/{vehicleId}/work-orders")
    public ResponseEntity<WorkOrderResponse> create(@PathVariable Long vehicleId,
                                                    @Valid @RequestBody CreateWorkOrderRequest request,
                                                    UriComponentsBuilder uriBuilder) {
        WorkOrderResponse created = workOrderService.createWorkOrder(vehicleId, request);
        URI location = uriBuilder.path("/api/v1/work-orders/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Lists all work orders, or only those of a given vehicle when {@code vehicleId}
     * is supplied as a query parameter.
     */
    @GetMapping("/api/v1/work-orders")
    public List<WorkOrderResponse> getAll(@RequestParam(required = false) Long vehicleId) {
        return vehicleId == null
                ? workOrderService.getAllWorkOrders()
                : workOrderService.getWorkOrdersByVehicle(vehicleId);
    }

    @GetMapping("/api/v1/work-orders/{id}")
    public WorkOrderResponse getById(@PathVariable Long id) {
        return workOrderService.getWorkOrderById(id);
    }

    @PutMapping("/api/v1/work-orders/{id}")
    public WorkOrderResponse update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateWorkOrderRequest request) {
        return workOrderService.updateWorkOrder(id, request);
    }

    @PatchMapping("/api/v1/work-orders/{id}/status")
    public WorkOrderResponse updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody UpdateWorkOrderStatusRequest request) {
        return workOrderService.updateStatus(id, request.status());
    }

    @DeleteMapping("/api/v1/work-orders/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        workOrderService.deleteWorkOrder(id);
    }
}
