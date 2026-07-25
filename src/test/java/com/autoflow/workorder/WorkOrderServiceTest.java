package com.autoflow.workorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.autoflow.common.exception.ResourceNotFoundException;
import com.autoflow.customer.Customer;
import com.autoflow.vehicle.FuelType;
import com.autoflow.vehicle.Vehicle;
import com.autoflow.vehicle.VehicleRepository;
import com.autoflow.workorder.dto.CreateWorkOrderRequest;
import com.autoflow.workorder.dto.WorkOrderResponse;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link WorkOrderService} with mocked repositories.
 */
@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Spy
    private WorkOrderMapper workOrderMapper;

    @InjectMocks
    private WorkOrderService workOrderService;

    private Vehicle vehicleWithOwner() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("Ola");
        customer.setLastName("Nordmann");

        Vehicle vehicle = new Vehicle();
        vehicle.setId(2L);
        vehicle.setRegistrationNumber("AB12345");
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setFuelType(FuelType.PETROL);
        vehicle.setCustomer(customer);
        return vehicle;
    }

    private CreateWorkOrderRequest request() {
        return new CreateWorkOrderRequest("Brake service", "Change brake pads",
                null, 45000, "Squeaking noise", null, null);
    }

    @Test
    void createWorkOrder_generatesNumberAndDefaults() {
        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(vehicleWithOwner()));
        when(workOrderRepository.nextWorkOrderNumberSequence()).thenReturn(42L);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> {
            WorkOrder wo = inv.getArgument(0);
            wo.setId(10L);
            return wo;
        });

        WorkOrderResponse response = workOrderService.createWorkOrder(2L, request());

        assertThat(response.workOrderNumber()).isEqualTo("WO-000042");
        assertThat(response.status()).isEqualTo(WorkOrderStatus.CREATED);
        assertThat(response.priority()).isEqualTo(WorkOrderPriority.NORMAL);
        assertThat(response.customerId()).isEqualTo(1L);
        assertThat(response.vehicleId()).isEqualTo(2L);
    }

    @Test
    void createWorkOrder_whenVehicleMissing_throwsNotFound() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workOrderService.createWorkOrder(99L, request()))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(workOrderRepository, never()).save(any(WorkOrder.class));
    }

    @Test
    void updateStatus_toCompleted_setsCompletedAt() {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setId(10L);
        workOrder.setWorkOrderNumber("WO-000001");
        workOrder.setStatus(WorkOrderStatus.IN_PROGRESS);
        workOrder.setPriority(WorkOrderPriority.NORMAL);
        workOrder.setVehicle(vehicleWithOwner());
        workOrder.setCustomer(vehicleWithOwner().getCustomer());
        when(workOrderRepository.findById(10L)).thenReturn(Optional.of(workOrder));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkOrderResponse response = workOrderService.updateStatus(10L, WorkOrderStatus.COMPLETED);

        assertThat(response.status()).isEqualTo(WorkOrderStatus.COMPLETED);
        assertThat(response.completedAt()).isNotNull();
    }
}
