package com.autoflow.workorder;

import com.autoflow.workorder.dto.CreateWorkOrderLineRequest;
import com.autoflow.workorder.dto.UpdateWorkOrderLineRequest;
import com.autoflow.workorder.dto.WorkOrderLineResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST endpoints for work order lines, nested under their work order.
 */
@RestController
@RequestMapping("/api/v1/work-orders/{workOrderId}/lines")
public class WorkOrderLineController {

    private final WorkOrderLineService lineService;

    public WorkOrderLineController(WorkOrderLineService lineService) {
        this.lineService = lineService;
    }

    @PostMapping
    public ResponseEntity<WorkOrderLineResponse> add(@PathVariable Long workOrderId,
                                                     @Valid @RequestBody CreateWorkOrderLineRequest request,
                                                     UriComponentsBuilder uriBuilder) {
        WorkOrderLineResponse created = lineService.addLine(workOrderId, request);
        URI location = uriBuilder.path("/api/v1/work-orders/{workOrderId}/lines/{id}")
                .buildAndExpand(workOrderId, created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    public List<WorkOrderLineResponse> getAll(@PathVariable Long workOrderId) {
        return lineService.getLines(workOrderId);
    }

    @PutMapping("/{lineId}")
    public WorkOrderLineResponse update(@PathVariable Long workOrderId,
                                        @PathVariable Long lineId,
                                        @Valid @RequestBody UpdateWorkOrderLineRequest request) {
        return lineService.updateLine(workOrderId, lineId, request);
    }

    @DeleteMapping("/{lineId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long workOrderId, @PathVariable Long lineId) {
        lineService.deleteLine(workOrderId, lineId);
    }
}
