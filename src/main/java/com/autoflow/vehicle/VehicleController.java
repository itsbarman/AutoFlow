package com.autoflow.vehicle;

import com.autoflow.vehicle.dto.CreateVehicleRequest;
import com.autoflow.vehicle.dto.UpdateVehicleRequest;
import com.autoflow.vehicle.dto.VehicleResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST endpoints for vehicles. Creation is scoped under a customer; the other
 * operations act on a vehicle by its own id.
 */
@RestController
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/api/v1/customers/{customerId}/vehicles")
    public ResponseEntity<VehicleResponse> create(@PathVariable Long customerId,
                                                  @Valid @RequestBody CreateVehicleRequest request,
                                                  UriComponentsBuilder uriBuilder) {
        VehicleResponse created = vehicleService.createVehicle(customerId, request);
        URI location = uriBuilder.path("/api/v1/vehicles/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Lists all vehicles, or only those of a given customer when {@code customerId}
     * is supplied as a query parameter.
     */
    @GetMapping("/api/v1/vehicles")
    public List<VehicleResponse> getAll(@RequestParam(required = false) Long customerId) {
        return customerId == null
                ? vehicleService.getAllVehicles()
                : vehicleService.getVehiclesByCustomer(customerId);
    }

    @GetMapping("/api/v1/vehicles/{id}")
    public VehicleResponse getById(@PathVariable Long id) {
        return vehicleService.getVehicleById(id);
    }

    @PutMapping("/api/v1/vehicles/{id}")
    public VehicleResponse update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateVehicleRequest request) {
        return vehicleService.updateVehicle(id, request);
    }

    @DeleteMapping("/api/v1/vehicles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
    }
}
