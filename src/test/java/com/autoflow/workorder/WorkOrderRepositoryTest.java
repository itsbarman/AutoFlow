package com.autoflow.workorder;

import static org.assertj.core.api.Assertions.assertThat;

import com.autoflow.customer.Customer;
import com.autoflow.customer.CustomerRepository;
import com.autoflow.support.PostgresContainerSupport;
import com.autoflow.vehicle.FuelType;
import com.autoflow.vehicle.Vehicle;
import com.autoflow.vehicle.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Repository tests for work orders against a real PostgreSQL container,
 * including the number sequence.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class WorkOrderRepositoryTest extends PostgresContainerSupport {

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Vehicle persistedVehicle() {
        Customer customer = new Customer();
        customer.setFirstName("Ola");
        customer.setLastName("Nordmann");
        customer.setPhoneNumber("12345678");
        customer = customerRepository.save(customer);

        Vehicle vehicle = new Vehicle();
        vehicle.setCustomer(customer);
        vehicle.setRegistrationNumber("AB12345");
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setFuelType(FuelType.PETROL);
        return vehicleRepository.save(vehicle);
    }

    private WorkOrder workOrder(Vehicle vehicle, String number) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.setWorkOrderNumber(number);
        workOrder.setVehicle(vehicle);
        workOrder.setCustomer(vehicle.getCustomer());
        workOrder.setTitle("Brake service");
        workOrder.setStatus(WorkOrderStatus.CREATED);
        workOrder.setPriority(WorkOrderPriority.NORMAL);
        return workOrder;
    }

    @Test
    void save_and_existsByVehicleId_work() {
        Vehicle vehicle = persistedVehicle();
        workOrderRepository.save(workOrder(vehicle, "WO-000001"));

        assertThat(workOrderRepository.existsByVehicleId(vehicle.getId())).isTrue();
        assertThat(workOrderRepository.findByVehicleIdOrderByIdAsc(vehicle.getId())).hasSize(1);
    }

    @Test
    void nextWorkOrderNumberSequence_returnsIncreasingValues() {
        long first = workOrderRepository.nextWorkOrderNumberSequence();
        long second = workOrderRepository.nextWorkOrderNumberSequence();

        assertThat(second).isGreaterThan(first);
    }
}
