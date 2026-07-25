package com.autoflow.workorder;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Data access for {@link WorkOrder}. Used only by the service layer.
 */
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    List<WorkOrder> findByVehicleIdOrderByIdAsc(Long vehicleId);

    List<WorkOrder> findByCustomerIdOrderByIdAsc(Long customerId);

    /** Used by the vehicle delete guard to block deleting a vehicle with work orders. */
    boolean existsByVehicleId(Long vehicleId);

    /** Draws the next value from the database sequence for the work order number. */
    @Query(value = "SELECT nextval('work_order_number_seq')", nativeQuery = true)
    long nextWorkOrderNumberSequence();
}
