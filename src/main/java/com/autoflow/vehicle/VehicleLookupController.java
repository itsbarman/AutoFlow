package com.autoflow.vehicle;

import com.autoflow.vehicle.dto.VehicleLookupResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proxy endpoint that looks up vehicle data from Statens Vegvesen by
 * registration number and returns a simplified response ready for the UI to
 * pre-fill the vehicle registration form.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleLookupController {

    private final VehicleLookupService vehicleLookupService;

    public VehicleLookupController(VehicleLookupService vehicleLookupService) {
        this.vehicleLookupService = vehicleLookupService;
    }

    /**
     * Looks up vehicle data from Statens Vegvesen.
     *
     * @param regnr registration number, e.g. {@code AB12345}
     */
    @GetMapping("/lookup")
    public VehicleLookupResponse lookup(@RequestParam String regnr) {
        return vehicleLookupService.lookup(regnr);
    }
}
