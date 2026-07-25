package com.autoflow.workorder;

import com.autoflow.common.exception.InvalidOperationException;
import com.autoflow.common.exception.ResourceNotFoundException;
import com.autoflow.workorder.dto.CreateWorkOrderLineRequest;
import com.autoflow.workorder.dto.UpdateWorkOrderLineRequest;
import com.autoflow.workorder.dto.WorkOrderLineResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for work order lines (parts/labor). Lines always belong to a
 * work order; the {@code workOrderId} in the path is verified against the line.
 */
@Service
@Transactional
public class WorkOrderLineService {

    private static final Logger log = LoggerFactory.getLogger(WorkOrderLineService.class);

    private final WorkOrderLineRepository lineRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderLineMapper lineMapper;

    public WorkOrderLineService(WorkOrderLineRepository lineRepository,
                                WorkOrderRepository workOrderRepository,
                                WorkOrderLineMapper lineMapper) {
        this.lineRepository = lineRepository;
        this.workOrderRepository = workOrderRepository;
        this.lineMapper = lineMapper;
    }

    public WorkOrderLineResponse addLine(Long workOrderId, CreateWorkOrderLineRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Arbeidsordre", workOrderId));
        WorkOrderLine line = lineMapper.toEntity(request, workOrder);
        WorkOrderLine saved = lineRepository.save(line);
        log.info("Line {} added to work order {}", saved.getId(), workOrder.getWorkOrderNumber());
        return lineMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderLineResponse> getLines(Long workOrderId) {
        if (!workOrderRepository.existsById(workOrderId)) {
            throw ResourceNotFoundException.of("Arbeidsordre", workOrderId);
        }
        return lineRepository.findByWorkOrderIdOrderByIdAsc(workOrderId).stream()
                .map(lineMapper::toResponse)
                .toList();
    }

    public WorkOrderLineResponse updateLine(Long workOrderId, Long lineId,
                                            UpdateWorkOrderLineRequest request) {
        WorkOrderLine line = findLineForWorkOrder(workOrderId, lineId);
        lineMapper.updateEntity(line, request);
        WorkOrderLine saved = lineRepository.save(line);
        log.info("Line {} updated", saved.getId());
        return lineMapper.toResponse(saved);
    }

    public void deleteLine(Long workOrderId, Long lineId) {
        WorkOrderLine line = findLineForWorkOrder(workOrderId, lineId);
        lineRepository.delete(line);
        log.info("Line {} deleted", lineId);
    }

    private WorkOrderLine findLineForWorkOrder(Long workOrderId, Long lineId) {
        WorkOrderLine line = lineRepository.findById(lineId)
                .orElseThrow(() -> ResourceNotFoundException.of("Varelinje", lineId));
        if (!line.getWorkOrder().getId().equals(workOrderId)) {
            throw new InvalidOperationException(
                    "Varelinjen tilhører ikke den angitte arbeidsordren");
        }
        return line;
    }
}
