package com.autoflow.vehicle;

import com.autoflow.common.exception.ResourceNotFoundException;
import com.autoflow.vehicle.dto.VehicleLookupResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Fetches vehicle data from the Statens Vegvesen public lookup API and maps the
 * response to a {@link VehicleLookupResponse}.
 *
 * <p>The API URL is configurable via {@code autoflow.vegvesen.lookup-url} so it
 * can be swapped when Maskinporten credentials become available for the
 * authenticated akfell-datautlevering endpoint.</p>
 *
 * <p>The response is parsed using a {@link JsonNode} tree rather than concrete
 * binding classes because Vegvesen has shipped multiple API versions with
 * different JSON shapes (flat vs. nested Autosys format). All field access is
 * defensive (null-safe).</p>
 */
@Service
public class VehicleLookupService {

    private static final Logger log = LoggerFactory.getLogger(VehicleLookupService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public VehicleLookupService(
            @Value("${autoflow.vegvesen.lookup-url:https://kjoretoyoppslag.atlas.vegvesen.no/kjennemerke}") String baseUrl,
            ObjectMapper objectMapper) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(8).toMillis());

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Looks up a vehicle by its registration number (kjennemerke).
     * The registration number is normalised (upper-case, no spaces) before
     * calling the API.
     *
     * @throws ResourceNotFoundException when Vegvesen returns no data for the plate
     */
    public VehicleLookupResponse lookup(String registrationNumber) {
        String normalized = registrationNumber.replaceAll("\\s", "").toUpperCase(Locale.ROOT);
        log.debug("Looking up vehicle data for registration number '{}'", normalized);

        String rawJson;
        try {
            rawJson = restClient.get()
                    .uri("/{regnr}", normalized)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.info("Vegvesen returned {} for registration number '{}'",
                    ex.getStatusCode(), normalized);
            throw new ResourceNotFoundException(
                    "Ingen kjøretøydata funnet for registreringsnummer " + normalized);
        } catch (RestClientException ex) {
            log.warn("Could not reach Vegvesen API for '{}': {}", normalized, ex.getMessage());
            throw new ResourceNotFoundException(
                    "Vegvesen-tjenesten er ikke tilgjengelig akkurat nå. Prøv igjen.");
        }

        return parse(rawJson, normalized);
    }

    // -------------------------------------------------------------------------
    // JSON parsing — handles both Autosys (nested) and flat response formats
    // -------------------------------------------------------------------------

    private VehicleLookupResponse parse(String json, String regnr) {
        try {
            JsonNode root = objectMapper.readTree(json);

            // Autosys (akfell-datautlevering) format: { "kjoretoydataListe": [ {...} ] }
            if (root.has("kjoretoydataListe")) {
                JsonNode vehicle = root.path("kjoretoydataListe").path(0);
                if (vehicle.isMissingNode()) {
                    throw new ResourceNotFoundException(
                            "Ingen kjøretøydata funnet for registreringsnummer " + regnr);
                }
                return parseAutosys(vehicle, regnr);
            }

            // kjoretoyoppslag flat format — fall through to flat parsing
            return parseFlat(root, regnr);

        } catch (ResourceNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Failed to parse Vegvesen response for '{}': {}", regnr, ex.getMessage());
            throw new ResourceNotFoundException(
                    "Kunne ikke tolke data fra Vegvesen for registreringsnummer " + regnr);
        }
    }

    /** Parses the nested Autosys format used by akfell-datautlevering. */
    private VehicleLookupResponse parseAutosys(JsonNode v, String regnr) {
        JsonNode tekniskeData = v
                .path("godkjenning")
                .path("tekniskGodkjenning")
                .path("tekniskeData");
        JsonNode generelt = tekniskeData.path("generelt");

        String make = text(generelt.path("merke").path(0).path("kodeNavn"));
        if (make == null) make = text(generelt.path("merke").path(0).path("merke"));
        String model = text(generelt.path("handelsbetegnelse").path(0));
        String vin = text(v.path("understellsnummer"));
        Integer modelYear = parseYear(v.path("forstegangsregistrering")
                .path("registrertForstegangNorgeDato").asText(null));
        String fuelText = text(tekniskeData
                .path("motorOgDrivverk").path("motor").path(0)
                .path("drivstoff").path(0).path("drivstoffKode").path("kodeNavn"));
        FuelType fuelType = mapFuel(fuelText);
        String color = text(v.path("godkjenning").path("tekniskGodkjenning")
                .path("tekniskeData").path("karosseriOgLasteplan")
                .path("rFarge").path(0).path("kodeNavn"));

        return new VehicleLookupResponse(regnr, capitalize(make), capitalize(model),
                modelYear, fuelType, vin, capitalize(color));
    }

    /**
     * Parses a flat response format as returned by kjoretoyoppslag.
     * Field names are guessed based on known Vegvesen naming conventions.
     */
    private VehicleLookupResponse parseFlat(JsonNode v, String regnr) {
        // Try several plausible field names
        String make = firstText(v, "merke", "kjoretoyserie", "bilmerke");
        String model = firstText(v, "handelsbetegnelse", "modell", "typebetegnelse");
        String vin = firstText(v, "understellsnummer", "vin");
        String fuelText = firstText(v, "drivstoff", "drivstofftype");
        FuelType fuelType = mapFuel(fuelText);
        String color = firstText(v, "farge", "kjoretoyFarge", "kjennetegnFarge");

        Integer modelYear = null;
        String dateStr = firstText(v, "forstegangsRegistrertDato",
                "registrertForstegangNorgeDato", "forstegangsregistreringDato");
        if (dateStr == null) {
            // Try year directly
            String yearStr = firstText(v, "modellAr", "produksjonsAr");
            if (yearStr != null) {
                modelYear = parseYear(yearStr + "-01-01");
            }
        } else {
            modelYear = parseYear(dateStr);
        }

        return new VehicleLookupResponse(regnr, capitalize(make), capitalize(model),
                modelYear, fuelType, vin, capitalize(color));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String text(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        String value = node.asText(null);
        return (value == null || value.isBlank() || value.equals("null")) ? null : value;
    }

    private String firstText(JsonNode parent, String... fields) {
        for (String field : fields) {
            String value = text(parent.path(field));
            if (value != null) return value;
        }
        return null;
    }

    private Integer parseYear(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            return Integer.parseInt(dateStr.substring(0, 4));
        } catch (Exception e) {
            return null;
        }
    }

    private String capitalize(String value) {
        if (value == null) return null;
        // "TOYOTA" → "Toyota", "AB CD" → "Ab Cd"
        String[] parts = value.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(Character.toUpperCase(part.charAt(0)));
                sb.append(part.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return sb.toString();
    }

    /**
     * Maps the Norwegian fuel type description from Vegvesen to our enum.
     * The mapping covers the values used in the Autosys system.
     */
    FuelType mapFuel(String fuel) {
        if (fuel == null) return FuelType.OTHER;
        String f = fuel.toUpperCase(Locale.ROOT);
        if (f.contains("LADBAR") || f.contains("PLUG") || f.contains("PHEV")) return FuelType.PLUG_IN_HYBRID;
        if (f.contains("HYBRID") && f.contains("ELEKTRISK")) return FuelType.PLUG_IN_HYBRID;
        if (f.contains("HYBRID")) return FuelType.HYBRID;
        if (f.contains("ELEKTRISK") || f.contains("ELECTRIC") || f.contains("BEV")) return FuelType.ELECTRIC;
        if (f.contains("BENSIN") || f.contains("PETROL") || f.contains("GASOLINE")) return FuelType.PETROL;
        if (f.contains("DIESEL") || f.contains("BIOD")) return FuelType.DIESEL;
        return FuelType.OTHER;
    }
}
