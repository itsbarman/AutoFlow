package com.autoflow.vehicle;

import com.autoflow.common.exception.ResourceNotFoundException;
import com.autoflow.vehicle.dto.VehicleLookupResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
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
 * Fetches vehicle data from Statens Vegvesen and maps it to a
 * {@link VehicleLookupResponse}.
 *
 * <p>Two modes of operation, chosen automatically:</p>
 * <ul>
 *   <li><strong>Maskinporten (authenticated)</strong> — used when
 *       {@code autoflow.vegvesen.maskinporten.client-id} is set. Calls the
 *       official {@code akfell-datautlevering} API which requires a Maskinporten
 *       Bearer token. This is the reliable production path.</li>
 *   <li><strong>Public fallback</strong> — used when Maskinporten is not
 *       configured. Calls {@code kjoretoyoppslag.atlas.vegvesen.no} which does
 *       not require authentication but may be unreliable.</li>
 * </ul>
 */
@Service
public class VehicleLookupService {

    private static final Logger log = LoggerFactory.getLogger(VehicleLookupService.class);

    /**
     * Authenticated Vegvesen API (requires Maskinporten Bearer token).
     * Returns the full Autosys JSON format.
     */
    private static final String AUTOSYS_URL =
            "https://akfell-datautlevering.atlas.vegvesen.no/kjoretoy/felles/datautlevering/enkeltoppslag/kjoretoydata";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final Optional<MaskinportenTokenService> maskinportenTokenService;
    private final boolean useMaskinporten;

    public VehicleLookupService(
            @Value("${autoflow.vegvesen.lookup-url:https://kjoretoyoppslag.atlas.vegvesen.no/kjennemerke}") String lookupUrl,
            ObjectMapper objectMapper,
            Optional<MaskinportenTokenService> maskinportenTokenService) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(10).toMillis());

        this.useMaskinporten = maskinportenTokenService.isPresent();
        this.maskinportenTokenService = maskinportenTokenService;
        this.objectMapper = objectMapper;

        if (useMaskinporten) {
            // Authenticated path — calls akfell-datautlevering with ?kjennemerke=
            this.restClient = RestClient.builder()
                    .baseUrl(AUTOSYS_URL)
                    .requestFactory(factory)
                    .build();
            log.info("VehicleLookupService using authenticated Maskinporten path");
        } else {
            // Public fallback path — calls kjoretoyoppslag/{regnr}
            this.restClient = RestClient.builder()
                    .baseUrl(lookupUrl)
                    .requestFactory(factory)
                    .build();
            log.info("VehicleLookupService using public fallback path ({}). "
                    + "Configure Maskinporten for reliable access.", lookupUrl);
        }
    }

    /**
     * Looks up a vehicle by registration number.
     *
     * @throws ResourceNotFoundException when no data is found or the service is unavailable
     */
    public VehicleLookupResponse lookup(String registrationNumber) {
        String normalized = registrationNumber.replaceAll("\\s", "").toUpperCase(Locale.ROOT);
        log.debug("Looking up vehicle data for registration number '{}' (maskinporten={})",
                normalized, useMaskinporten);

        String rawJson = fetch(normalized);
        return parse(rawJson, normalized);
    }

    private String fetch(String regnr) {
        try {
            RestClient.RequestHeadersSpec<?> request;
            if (useMaskinporten) {
                String token = maskinportenTokenService.get().getAccessToken();
                request = restClient.get()
                        .uri("?kjennemerke={regnr}", regnr)
                        .header("Authorization", "Bearer " + token);
            } else {
                request = restClient.get().uri("/{regnr}", regnr);
            }
            return request.retrieve().body(String.class);

        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResourceNotFoundException(
                    "Ingen kjøretøydata funnet for registreringsnummer " + regnr
                    + ". Sjekk at registreringsnummeret er riktig.");

        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden ex) {
            log.error("Vegvesen API: authentication failed. Check Maskinporten credentials.");
            throw new ResourceNotFoundException(
                    "Autentisering mot Vegvesen feilet. Sjekk Maskinporten-konfigurasjon.");

        } catch (HttpClientErrorException ex) {
            log.info("Vegvesen returned {} for '{}'", ex.getStatusCode(), regnr);
            throw new ResourceNotFoundException(
                    "Ingen kjøretøydata funnet for registreringsnummer " + regnr + ".");

        } catch (HttpServerErrorException ex) {
            // The kjoretoyoppslag public API returns 500 both for "not found" and real errors.
            // We cannot distinguish them, so we provide a helpful message.
            log.warn("Vegvesen returned {} for '{}'. This may be a 'not found' or a "
                    + "temporary server error. Configure Maskinporten for reliable access.",
                    ex.getStatusCode(), regnr);
            if (useMaskinporten) {
                throw new ResourceNotFoundException(
                        "Ingen kjøretøydata funnet for registreringsnummer " + regnr + ".");
            } else {
                throw new ResourceNotFoundException(
                        "Vegvesen-tjenesten er midlertidig utilgjengelig (500). "
                        + "Konfigurer Maskinporten for pålitelig tilgang, "
                        + "eller prøv igjen senere.");
            }

        } catch (RestClientException ex) {
            log.warn("Could not reach Vegvesen API for '{}': {}", regnr, ex.getMessage());
            throw new ResourceNotFoundException(
                    "Vegvesen-tjenesten er ikke tilgjengelig akkurat nå. Prøv igjen om litt.");
        }
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
        String color = text(tekniskeData.path("karosseriOgLasteplan")
                .path("rFarge").path(0).path("kodeNavn"));

        return new VehicleLookupResponse(regnr, capitalize(make), capitalize(model),
                modelYear, fuelType, vin, capitalize(color));
    }

    /**
     * Parses a flat response format as returned by kjoretoyoppslag.
     * Field names are guessed based on known Vegvesen naming conventions.
     */
    private VehicleLookupResponse parseFlat(JsonNode v, String regnr) {
        String make = firstText(v, "merke", "kjoretoyserie", "bilmerke");
        String model = firstText(v, "handelsbetegnelse", "modell", "typebetegnelse");
        String vin = firstText(v, "understellsnummer", "vin");
        String fuelText = firstText(v, "drivstoff", "drivstofftype");
        FuelType fuelType = mapFuel(fuelText);
        String color = firstText(v, "farge", "kjoretoyFarge");

        Integer modelYear = null;
        String dateStr = firstText(v, "forstegangsRegistrertDato",
                "registrertForstegangNorgeDato", "forstegangsregistreringDato");
        if (dateStr == null) {
            String yearStr = firstText(v, "modellAr", "produksjonsAr");
            if (yearStr != null) modelYear = parseYear(yearStr + "-01-01");
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
