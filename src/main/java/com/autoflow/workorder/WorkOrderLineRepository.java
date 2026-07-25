package com.autoflow.workorder;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkOrderLineRepository extends JpaRepository<WorkOrderLine, Long> {

    List<WorkOrderLine> findByWorkOrderIdOrderByIdAsc(Long workOrderId);
}
