package com.autoflow.vehicle;

import com.autoflow.common.exception.DuplicateResourceException;
import com.autoflow.common.exception.InvalidOperationException;
import com.autoflow.common.exception.ResourceNotFoundException;
import com.autoflow.customer.Customer;
import com.autoflow.customer.CustomerRepository;
import com.autoflow.vehicle.dto.CreateVehicleRequest;
import com.autoflow.vehicle.dto.UpdateVehicleRequest;
import com.autoflow.vehicle.dto.VehicleResponse;
import com.autoflow.workorder.WorkOrderRepository;
import java.time.Year;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for vehicles: normalization, uniqueness checks and the link
 * to the owning customer. Controllers never touch the repository directly.
 */
@Service
@Transactional
public class VehicleService {

    private static final Logger log = LoggerFactory.getLogger(VehicleService.class);

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final VehicleMapper vehicleMapper;
    private final WorkOrderRepository workOrderRepository;

    public VehicleService(VehicleRepository vehicleRepository,
                          CustomerRepository customerRepository,
                          VehicleMapper vehicleMapper,
                          WorkOrderRepository workOrderRepository) {
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.vehicleMapper = vehicleMapper;
        this.workOrderRepository = workOrderRepository;
    }

    public VehicleResponse createVehicle(Long customerId, CreateVehicleRequest request) {
        Customer owner = customerRepository.findById(customerId)
                .orElseThrow(() -> ResourceNotFoundException.of("Customer", customerId));

        String regNumber = normalizeRegistrationNumber(request.registrationNumber());
        validateModelYear(request.modelYear());
        String vin = normalizeVin(request.vin());

        if (vehicleRepository.existsByRegistrationNumber(regNumber)) {
            throw new DuplicateResourceException(
                    "A vehicle with registration number " + regNumber + " already exists");
        }
        if (vin != null && vehicleRepository.existsByVin(vin)) {
            throw new DuplicateResourceException("A vehicle with VIN " + vin + " already exists");
        }

        Vehicle vehicle = vehicleMapper.toEntity(request, owner, regNumber);
        vehicle.setVin(vin);
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle {} registered ({}) for customer {}", saved.getId(), regNumber, customerId);
        return vehicleMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream().map(vehicleMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByCustomer(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw ResourceNotFoundException.of("Customer", customerId);
        }
        return vehicleRepository.findByCustomerIdOrderByIdAsc(customerId).stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        return vehicleMapper.toResponse(findVehicleOrThrow(id));
    }

    public VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = findVehicleOrThrow(id);

        String regNumber = normalizeRegistrationNumber(request.registrationNumber());
        validateModelYear(request.modelYear());
        String vin = normalizeVin(request.vin());

        if (vehicleRepository.existsByRegistrationNumberAndIdNot(regNumber, id)) {
            throw new DuplicateResourceException(
                    "A vehicle with registration number " + regNumber + " already exists");
        }
        if (vin != null && vehicleRepository.existsByVinAndIdNot(vin, id)) {
            throw new DuplicateResourceException("A vehicle with VIN " + vin + " already exists");
        }

        vehicleMapper.updateEntity(vehicle, request, regNumber);
        vehicle.setVin(vin);
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle {} updated", saved.getId());
        return vehicleMapper.toResponse(saved);
    }

    public void deleteVehicle(Long id) {
        Vehicle vehicle = findVehicleOrThrow(id);
        // Business data must not disappear implicitly: a vehicle with work orders
        // cannot be deleted.
        if (workOrderRepository.existsByVehicleId(id)) {
            log.info("Rejected deletion of vehicle {} because it still has work orders", id);
            throw new InvalidOperationException(
                    "Vehicle cannot be deleted while it still has work orders");
        }
        vehicleRepository.delete(vehicle);
        log.info("Vehicle {} deleted", id);
    }

    private Vehicle findVehicleOrThrow(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Vehicle", id));
    }

    /** Uppercase and strip all whitespace, e.g. "ab 12345" -> "AB12345". */
    private String normalizeRegistrationNumber(String value) {
        return value.replaceAll("\\s", "").toUpperCase();
    }

    /** Blank VIN is treated as "not provided" (null). */
    private String normalizeVin(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.replaceAll("\\s", "").toUpperCase();
        return normalized.isEmpty() ? null : normalized;
    }

    private void validateModelYear(Integer modelYear) {
        if (modelYear == null) {
            return;
        }
        int nextYear = Year.now().getValue() + 1;
        if (modelYear > nextYear) {
            throw new InvalidOperationException(
                    "Model year cannot be later than " + nextYear);
        }
    }
}
